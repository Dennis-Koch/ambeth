package de.osthus.ambeth.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.Statement;
import java.util.List;

import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.RowChangeDescription;
import oracle.jdbc.dcn.RowChangeDescription.RowOperation;
import oracle.jdbc.dcn.TableChangeDescription;
import oracle.sql.ROWID;
import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.util.IConversionHelper;

public class OracleDatabaseChangeListener implements DatabaseChangeListener
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Autowired
	protected ITransaction transaction;

	@Override
	public void onDatabaseChangeNotification(final DatabaseChangeEvent databaseChangeEvent)
	{
		try
		{
			DataChangeEvent dataChangeEvent = transaction.processAndCommit(new ResultingDatabaseCallback<DataChangeEvent>()
			{
				@Override
				public DataChangeEvent callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
				{
					return executeInTransaction(databaseChangeEvent);
				}
			});
			eventDispatcher.dispatchEvent(dataChangeEvent);
		}
		catch (Throwable e)
		{
			// This handler will be called by the oracle driver. No need to throw the exception further which might risk a driver bug of any kind
			log.error(e);
		}
		finally
		{
			threadLocalCleanupController.cleanupThreadLocal();
		}
	}

	protected DataChangeEvent executeInTransaction(DatabaseChangeEvent databaseChangeEvent) throws Exception
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		IConversionHelper conversionHelper = this.conversionHelper;
		IDatabase database = this.database.getCurrent();
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		ISqlBuilder sqlBuilder = this.sqlBuilder;
		Connection connection = database.getAutowiredBeanInContext(Connection.class);

		Statement stm = connection.createStatement();
		PreparedStatement rowIdPstm = null;
		ResultSet rs = null;
		try
		{
			DataChangeEvent dcEvent = DataChangeEvent.create(-1, -1, -1);
			dcEvent.setLocalSource(false);

			TableChangeDescription[] tcDescs = databaseChangeEvent.getTableChangeDescription();
			for (TableChangeDescription tcDesc : tcDescs)
			{
				String tableName = tcDesc.getTableName();
				ITableMetaData table = databaseMetaData.getTableByName(tableName);
				if (table == null)
				{
					continue;
				}
				Class<?> entityType = table.getEntityType();
				if (entityType == null)
				{
					continue;
				}
				RowChangeDescription[] rcDescs = tcDesc.getRowChangeDescription();
				if (rcDescs.length == 0)
				{
					continue;
				}
				String idFieldName = table.getIdField().getName();
				String versionFieldName = table.getVersionField() != null ? table.getVersionField().getName() : null;
				// ArrayList<ROWID> rowIds = new ArrayList<ROWID>();
				List<RowId> rowIds = new java.util.ArrayList<RowId>();
				HashMap<RowIdKey, RowOperation> rowIdToRowOperationMap = new HashMap<RowIdKey, RowOperation>();
				AppendableStringBuilder sb = tlObjectCollector.create(AppendableStringBuilder.class);
				sb.append("SELECT ");
				sqlBuilder.appendName("ROWID", sb);
				sb.append(',');
				sqlBuilder.appendName(idFieldName, sb);
				if (versionFieldName != null)
				{
					sb.append(',');
					sqlBuilder.appendName(versionFieldName, sb);
				}
				sb.append(" FROM ");
				sqlBuilder.appendName(tableName, sb);
				sb.append(" WHERE ");
				sqlBuilder.appendName("ROWID", sb);
				sb.append(" IN (");

				for (RowChangeDescription rcDesc : rcDescs)
				{
					RowId rowId = rcDesc.getRowid();

					if (rowIds.size() > 0)
					{
						sb.append(',');
					}
					sb.append('?');
					rowIds.add(rowId);

					rowIdToRowOperationMap.put(new RowIdKey(rowId.getBytes()), rcDesc.getRowOperation());
				}
				sb.append(')');
				rowIdPstm = connection.prepareStatement(sb.toString());
				for (int a = rowIds.size(); a-- > 0;)
				{
					rowIdPstm.setRowId(a + 1, rowIds.get(a));
				}
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
				Class<?> idType = metaData.getIdMember().getRealType();
				Class<?> versionType = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;

				rs = rowIdPstm.executeQuery();
				while (rs.next())
				{
					ROWID rowId = (ROWID) rs.getRowId("ROWID");

					RowOperation rowOperation = rowIdToRowOperationMap.get(new RowIdKey(rowId.getBytes()));

					Object id = rs.getObject(idFieldName);
					Object version = versionFieldName != null ? rs.getObject(versionFieldName) : null;

					id = conversionHelper.convertValueToType(idType, id);
					version = conversionHelper.convertValueToType(versionType, version);

					DataChangeEntry dataChangeEntry = new DataChangeEntry(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version);

					switch (rowOperation)
					{
						case INSERT:
							dcEvent.getInserts().add(dataChangeEntry);
							break;
						case UPDATE:
							dcEvent.getUpdates().add(dataChangeEntry);
							break;
						case DELETE:
							dcEvent.getDeletes().add(dataChangeEntry);
							break;
						default:
							throw new IllegalStateException("Enum " + rowOperation + " not supported");
					}
				}
				tlObjectCollector.dispose(sb);
			}
			return dcEvent;
		}
		finally
		{
			JdbcUtil.close(rowIdPstm, rs);
			JdbcUtil.close(stm);
		}
	}
}
