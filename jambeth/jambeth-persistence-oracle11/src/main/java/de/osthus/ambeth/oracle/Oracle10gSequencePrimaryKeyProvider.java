package de.osthus.ambeth.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.orm.XmlDatabaseMapper;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.sql.AbstractCachingPrimaryKeyProvider;

public class Oracle10gSequencePrimaryKeyProvider extends AbstractCachingPrimaryKeyProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Override
	protected void acquireIdsIntern(ITableMetaData table, int count, List<Object> targetIdList)
	{
		String[] schemaAndName = XmlDatabaseMapper.splitSchemaAndName(table.getSequenceName());
		if (schemaAndName[0] == null)
		{
			// if no schema is explicitly specified in the sequence we look in the schema of the table
			schemaAndName[0] = XmlDatabaseMapper.splitSchemaAndName(table.getFullqualifiedEscapedName())[0];
		}
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try
		{
			pstm = connection.prepareStatement("SELECT " + connectionDialect.escapeSchemaAndSymbolName(schemaAndName[0], schemaAndName[1])
					+ ".nextval FROM DUAL CONNECT BY level<=?");
			pstm.setInt(1, count);
			rs = pstm.executeQuery();
			while (rs.next())
			{
				Object id = rs.getObject(1); // We have only 1 column in the select so it is ok to retrieve it by the unique id
				targetIdList.add(id);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(pstm, rs);
		}
	}
}
