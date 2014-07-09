package de.osthus.ambeth.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import de.osthus.ambeth.IDatabasePool;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.database.IDatabaseProvider;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.ILoggerHistory;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.PersistenceWarnUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.ITypeInfo;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringConversionHelper;

public class Database implements IDatabase, IConfigurableDatabase, IInitializingBean, IStartingBean, IDisposableBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IContextProvider contextProvider;

	@Autowired
	protected IDatabaseProvider databaseProvider;

	@Autowired
	protected ILoggerHistory loggerHistory;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	protected IModifyingDatabase modifyingDatabase;

	@Autowired
	protected IDatabasePool pool;

	@Autowired
	protected IRelationProvider relationProvider;

	@Autowired
	protected IServiceContext serviceContext;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	protected final HashMap<String, ITable> nameToTableDict = new HashMap<String, ITable>();
	protected final HashMap<Class<?>, ITable> typeToTableDict = new HashMap<Class<?>, ITable>();
	protected final HashMap<Class<?>, ITable> typeToArchiveTableDict = new HashMap<Class<?>, ITable>();
	protected final HashMap<String, ILink> nameToLinkDict = new HashMap<String, ILink>();
	protected final HashMap<String, ILink> definingNameToLinkDict = new HashMap<String, ILink>();
	protected final HashMap<TablesMapKey, List<ILink>> tablesToLinkDict = new HashMap<TablesMapKey, List<ILink>>();
	protected final IdentityHashSet<IField> fieldsMappedToLinks = new IdentityHashSet<IField>();
	protected final HashMap<Class<?>, IEntityHandler> typeToEntityHandler = new HashMap<Class<?>, IEntityHandler>();
	protected final ArrayList<Class<?>> handledEntities = new ArrayList<Class<?>>();
	protected final DefaultExtendableContainer<IDatabaseDisposeHook> databaseDisposeHooks = new DefaultExtendableContainer<IDatabaseDisposeHook>(
			IDatabaseDisposeHook.class, "databaseDisposeHook");

	protected long sessionId;
	protected String name;
	protected final ArrayList<ITable> tables = new ArrayList<ITable>();
	protected final List<ILink> links = new ArrayList<ILink>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public void afterStarted()
	{
		List<IField> alternateIdMembers = new ArrayList<IField>();
		List<ITable> tables = getTables();
		for (int tableIndex = tables.size(); tableIndex-- > 0;)
		{
			ITable table = tables.get(tableIndex);
			Class<?> fromType = table.getEntityType();
			if (fromType == null)
			{
				if (log.isWarnEnabled())
				{
					PersistenceWarnUtil.logWarnOnce(log, loggerHistory, connection, "No entity mapped to table '" + table.getName() + "'");
				}
				continue;
			}
			if (!table.isArchive())
			{
				typeToTableDict.put(fromType, table);
			}
			else
			{
				typeToArchiveTableDict.put(fromType, table);
				continue;
			}

			ITypeInfo typeInfo = typeInfoProvider.getTypeInfo(fromType);
			List<IDirectedLink> links = table.getLinks();
			for (int linkIndex = links.size(); linkIndex-- > 0;)
			{
				IDirectedLink link = links.get(linkIndex);
				ITable toTable = link.getToTable();

				Class<?> toType;
				if (toTable != null)
				{
					toType = toTable.getEntityType();
					if (toType == null)
					{
						if (log.isWarnEnabled())
						{
							log.warn("No entity mapped to table '" + toTable.getName() + "'. Error occured while handling link '" + link.getName() + "'");
						}
						continue;
					}
				}
				else
				{
					toType = link.getToEntityType();
				}
				String memberName = table.getMemberNameByLinkName(link.getName());
				if (!(memberName == null || memberName.isEmpty()))
				{
					continue;
				}
				ITypeInfoItem matchingMember = null;
				String typeNamePluralLower = null;
				for (ITypeInfoItem member : typeInfo.getMembers())
				{
					Class<?> elementType = member.getElementType();
					if (table.isIgnoredMember(member.getName()))
					{
						continue;
					}
					if (!relationProvider.isEntityType(member.getElementType()))
					{
						continue;
					}
					if (!elementType.isAssignableFrom(toType))
					{
						if (Collection.class.isAssignableFrom(elementType))
						{
							// No generic info at runtime, so we guess by name.
							if (typeNamePluralLower == null)
							{
								typeNamePluralLower = StringConversionHelper.entityNameToPlural(objectCollector,
										typeInfoProvider.getTypeInfo(toType).getSimpleName()).toLowerCase();
							}
							String memberNameLower = member.getName().toLowerCase();
							if (memberNameLower.equals(typeNamePluralLower))
							{
								matchingMember = member;
							}
						}
						continue;
					}
					// Check if this member is already configured to another link
					if (table.getLinkByMemberName(member.getName()) != null)
					{
						continue;
					}
					if (matchingMember != null)
					{
						// ambiguous property-to-entity relationship so we do nothing automatically here
						throw new IllegalArgumentException("Ambiguous property-to-entity relationship. Automatic mapping for link '" + link.getName()
								+ "' not possible! Multiple properties with the same expected relationtype found: " + matchingMember.toString() + " vs. "
								+ member.toString() + " on entity '" + table.getEntityType().getName() + "'");
					}
					matchingMember = member;
				}
				if (matchingMember == null)
				{
					if (!(memberName == null || memberName.isEmpty()))
					{
						throw new IllegalArgumentException("Property-to-entity relationship which is explicit defined for member '" + fromType.getName() + "."
								+ memberName + "' not possible. Member not found");
					}
					continue;
				}
				((Table) link.getFromTable()).mapLink(link.getName(), matchingMember.getName());
			}
			// TODO Reactivate this check with embedded-type case handling
			// for (ITypeInfoItem member : typeInfo.getChildMembers())
			// {
			// if (member.isXMLIgnore())
			// {
			// continue;
			// }
			// String memberName = member.getName();
			// if (table.getFieldByMemberName(memberName) == null && table.getLinkByMemberName(memberName) == null)
			// {
			// throw new IllegalArgumentException("Member '" + fromType.getName() + "." + memberName
			// + " is neither mapped to a link or a field and it is not annotated with " + Transient.class.getName());
			// }
			// }

			// Remove not-mapped (and so not usable) alternate id fields
			for (IField alternateIdMember : table.getAlternateIdFields())
			{
				if (alternateIdMember.getMember() != null)
				{
					alternateIdMembers.add(alternateIdMember);
				}
			}
			((Table) table).setAlternateIdFields(alternateIdMembers.toArray(new IField[alternateIdMembers.size()]));
			alternateIdMembers.clear();
		}
	}

	@Override
	public void registerDisposeHook(IDatabaseDisposeHook disposeHook)
	{
		databaseDisposeHooks.register(disposeHook);
	}

	@Override
	public void unregisterDisposeHook(IDatabaseDisposeHook disposeHook)
	{
		databaseDisposeHooks.unregister(disposeHook);
	}

	@Override
	public boolean isDisposed()
	{
		return serviceContext.isDisposed();
	}

	@Override
	public <T> T getAutowiredBeanInContext(Class<T> autowiredType)
	{
		return serviceContext.getService(autowiredType, false);
	}

	@Override
	public <T> T getNamedBeanInContext(String beanName, Class<T> expectedType)
	{
		return serviceContext.getService(beanName, expectedType, false);
	}

	@Override
	public List<Class<?>> getHandledEntities()
	{
		return handledEntities;
	}

	@Override
	public IContextProvider getContextProvider()
	{
		return contextProvider;
	}

	@Override
	public IDatabasePool getPool()
	{
		return pool;
	}

	public IServiceContext getServiceProvider()
	{
		return serviceContext;
	}

	public Map<Class<?>, IEntityHandler> getTypeToEntityHandler()
	{
		return typeToEntityHandler;
	}

	@Override
	public void acquired(boolean readOnly)
	{
		try
		{
			connection.setReadOnly(readOnly);
		}
		catch (SQLException e)
		{
			// Intended blank
		}
		contextProvider.acquired();
		contextProvider.setCurrentTime(Long.valueOf(System.currentTimeMillis()));
	}

	@Override
	public void flushAndRelease()
	{
		ThreadLocal<IDatabase> databaseLocal = databaseProvider.getDatabaseLocal();
		IDatabase currentDatabase = databaseLocal.get();
		if (currentDatabase instanceof Factory)
		{
			Callback interceptor = ((Factory) currentDatabase).getCallback(0);

			currentDatabase = (IDatabase) ((ICascadedInterceptor) interceptor).getTarget();
		}
		if (this == currentDatabase)
		{
			databaseLocal.remove();
		}
		try
		{
			flush();
		}
		finally
		{
			clear();
			if (pool != null)
			{
				pool.releaseDatabase(this, true);
			}
		}
	}

	@Override
	public void release(boolean errorOccured)
	{
		ThreadLocal<IDatabase> databaseLocal = databaseProvider.getDatabaseLocal();
		IDatabase currentDatabase = databaseLocal.get();
		if (currentDatabase instanceof Factory)
		{
			Callback interceptor = ((Factory) currentDatabase).getCallback(0);

			currentDatabase = (IDatabase) ((ICascadedInterceptor) interceptor).getTarget();
		}
		if (this == currentDatabase)
		{
			databaseLocal.remove();
		}
		clear();
		if (pool != null)
		{
			pool.releaseDatabase(this, !errorOccured);
		}
	}

	@Override
	public void destroy() throws Throwable
	{
		connection = null;
		ThreadLocal<IDatabase> databaseLocal = databaseProvider.getDatabaseLocal();
		IDatabase currentDatabase = databaseLocal.get();
		if (currentDatabase instanceof Factory)
		{
			Callback interceptor = ((Factory) currentDatabase).getCallback(0);

			currentDatabase = (IDatabase) ((ICascadedInterceptor) interceptor).getTarget();
		}
		if (this == currentDatabase)
		{
			databaseLocal.remove();
		}
		clear();
		for (IDatabaseDisposeHook disposeHook : databaseDisposeHooks.getExtensions())
		{
			disposeHook.databaseDisposed(this);
		}
	}

	protected void clear()
	{
		contextProvider.clear();
	}

	@Override
	public void dispose()
	{
		serviceContext.dispose();
	}

	@Override
	public IDatabase getCurrent()
	{
		return this;
	}

	@Override
	public long getSessionId()
	{
		return sessionId;
	}

	@Override
	public void setSessionId(long sessionId)
	{
		this.sessionId = sessionId;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String[] getSchemaNames()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public List<ITable> getTables()
	{
		return tables;
	}

	@Override
	public List<ILink> getLinks()
	{
		return links;
	}

	@Override
	public ITable mapTable(String tableName, Class<?> entityType)
	{
		ITable table = getTableByName(tableName);
		if (table == null)
		{
			throw new IllegalArgumentException("No table with name '" + tableName + "' found");
		}
		Class<?> mappedEntityType = table.getEntityType();
		if (mappedEntityType != null && !mappedEntityType.equals(entityType))
		{
			throw new IllegalArgumentException("Table '" + tableName + "' already mapped to entity '"
					+ typeInfoProvider.getTypeInfo(mappedEntityType).getSimpleName() + "'");
		}
		((Table) table).setEntityType(entityType);
		return table;
	}

	@Override
	public ITable mapArchiveTable(String tableName, Class<?> entityType)
	{
		ITable table = getTableByName(tableName);
		if (table == null)
		{
			throw new IllegalArgumentException("No table with name '" + tableName + "' found");
		}
		Class<?> mappedEntityType = table.getEntityType();
		if (mappedEntityType != null && !mappedEntityType.equals(entityType))
		{
			throw new IllegalArgumentException("Table '" + tableName + "' already mapped to entity '"
					+ typeInfoProvider.getTypeInfo(mappedEntityType).getSimpleName() + "'");
		}
		Table tableInst = (Table) table;
		tableInst.setEntityType(entityType);
		tableInst.setArchive(true);
		return table;
	}

	@Override
	public ITable getTableByName(String tableName)
	{
		return nameToTableDict.get(tableName);
	}

	@Override
	public ILink getLinkByName(String linkName)
	{
		return nameToLinkDict.get(linkName);
	}

	@Override
	public ILink getLinkByDefiningName(String definingName)
	{
		return definingNameToLinkDict.get(definingName);
	}

	@Override
	public void addLinkByTables(ILink link)
	{
		ITable fromTable = link.getFromTable();
		ITable toTable = link.getToTable();
		if (fromTable == null || toTable == null)
		{
			return;
		}

		TablesMapKey tablesMapKey = new TablesMapKey(fromTable, toTable);
		List<ILink> links = tablesToLinkDict.get(tablesMapKey);
		if (links == null)
		{
			links = new ArrayList<ILink>();
			tablesToLinkDict.put(tablesMapKey, links);
		}
		links.add(link);
	}

	@Override
	public List<ILink> getLinksByTables(ITable table1, ITable table2)
	{
		TablesMapKey tablesMapKey = new TablesMapKey(table1, table2);
		return tablesToLinkDict.get(tablesMapKey);
	}

	@Override
	public ITable getTableByType(Class<?> entityType)
	{
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ITable table = typeToTableDict.get(entityType);
		if (table == null)
		{
			throw new IllegalStateException("No table found for entity type '" + entityType.getName() + "'");
		}
		return table;
	}

	@Override
	public ITable getArchiveTableByType(Class<?> entityType)
	{
		return typeToArchiveTableDict.get(entityType);
	}

	@Override
	public void flush()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void revert()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void revert(Savepoint savepoint)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean test()
	{
		return true;
	}

	public String createForeignKeyLinkName(String fromTableName, String fromFieldName, String toTableName, String toFieldName)
	{
		return "LINK_" + fromTableName + "_" + fromFieldName + "_" + toTableName + "_" + toFieldName;
	}

	@Override
	public Savepoint setSavepoint()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void rollback(Savepoint savepoint)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IList<String[]> disableConstraints()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void enableConstraints(IList<String[]> disabled)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean isLinkArchiveTable(String tableName)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ILink mapLink(ILink link)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean isFieldNullable(IField field) throws SQLException
	{
		throw new UnsupportedOperationException("Not implemented");
	}
}
