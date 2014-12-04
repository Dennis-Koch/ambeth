package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.IDatabaseMappedListener;
import de.osthus.ambeth.database.IDatabaseMappedListenerExtendable;
import de.osthus.ambeth.database.IDatabaseMapper;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.IExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.PersistenceWarnUtil;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.orm.IOrmPatternMatcher;
import de.osthus.ambeth.orm.XmlDatabaseMapper;
import de.osthus.ambeth.persistence.Database;
import de.osthus.ambeth.persistence.DirectedExternalLink;
import de.osthus.ambeth.persistence.DirectedLink;
import de.osthus.ambeth.persistence.Field;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ILink;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.ISavepoint;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.PermissionGroup;
import de.osthus.ambeth.persistence.Table;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.sql.IPrimaryKeyProvider;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.sql.ISqlConnection;
import de.osthus.ambeth.sql.ISqlKeywordRegistry;
import de.osthus.ambeth.sql.SqlField;
import de.osthus.ambeth.sql.SqlLink;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.NamedItemComparator;
import de.osthus.ambeth.util.ParamChecker;

public class JDBCDatabaseWrapper extends Database implements IDatabaseMappedListenerExtendable
{
	@LogInstance
	private ILogger log;

	private static final NamedItemComparator namedItemComparator = new NamedItemComparator();

	protected static final Pattern recycleBin = Pattern.compile("BIN\\$.{22}==\\$0", Pattern.CASE_INSENSITIVE);

	@Autowired
	protected ISqlConnection sqlConnection;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired
	protected ISqlKeywordRegistry sqlKeywordRegistry;

	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Autowired
	protected IOrmPatternMatcher ormPatternMatcher;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IPrimaryKeyProvider primaryKeyProvider;

	protected final IExtendableContainer<IDatabaseMappedListener> listeners = new DefaultExtendableContainer<IDatabaseMappedListener>(
			IDatabaseMappedListener.class, "databaseMappedListener");

	protected String schemaName;

	protected String[] schemaNames;

	protected long lastTestTime = System.currentTimeMillis(), trustTime = 10000;

	protected boolean singleSchema;

	protected String defaultVersionFieldName;

	protected String defaultCreatedByFieldName;

	protected String defaultCreatedOnFieldName;

	protected String defaultUpdatedByFieldName;

	protected String defaultUpdatedOnFieldName;

	protected Class<? extends SqlLink> linkType;

	protected final HashSet<String> viewNames = new HashSet<String>();

	protected final HashSet<String> ignoredTables = new HashSet<String>();

	protected final HashSet<String> linkArchiveTables = new HashSet<String>();

	// TODO JH 2012-07-10 temporary solution
	protected Map<String, Object> cachedSchemaInfos = null;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(schemaName, "schemaName");

		if (linkType == null)
		{
			linkType = SqlLink.class;
		}

		if (cachedSchemaInfos == null)
		{
			cachedSchemaInfos = new HashMap<String, Object>();
		}

		schemaName = schemaName.toUpperCase();
		schemaNames = schemaName.split(":");
		singleSchema = schemaNames.length == 1;

		LinkedHashMap<String, List<String[]>> linkNameToEntryMap = new LinkedHashMap<String, List<String[]>>();
		Set<String> fkFields = new HashSet<String>();
		Set<String> fqTableNames = new LinkedHashSet<String>();
		Set<String> fqDataTableNames = new LinkedHashSet<String>();
		final Map<String, List<SqlField>> tableNameToFields = new HashMap<String, List<SqlField>>();
		Map<String, List<String>> tableNameToPkFieldsMap = new HashMap<String, List<String>>();

		registerSqlKeywords();

		// Load required database metadata
		loadLinkInfos(linkNameToEntryMap, fkFields);
		loadTableNames(fqTableNames);
		loadTableFields(fqTableNames, tableNameToFields);
		grepDataTables(fqTableNames, fqDataTableNames, tableNameToFields, linkNameToEntryMap);
		mapPrimaryKeys(fqDataTableNames, tableNameToPkFieldsMap);

		if (fqDataTableNames.isEmpty() && log.isWarnEnabled())
		{
			PersistenceWarnUtil.logWarnOnce(log, loggerHistory, connection, "Schema '" + schemaName + "' contains no data tables");
		}

		for (String fqTableName : fqDataTableNames)
		{
			if (log.isDebugEnabled())
			{
				PersistenceWarnUtil.logDebugOnce(log, loggerHistory, connection, "Recognizing table " + fqTableName
						+ " as data table waiting for entity mapping");
			}
			JdbcTable table = new JdbcTable();
			table.setInitialVersion(Integer.valueOf(1));
			table.setName(fqTableName);
			table.setFullqualifiedEscapedName(XmlDatabaseMapper.escapeName(fqTableName));
			table.setViewBased(viewNames.contains(fqTableName));

			List<String> pkFieldNames = tableNameToPkFieldsMap.get(fqTableName);
			List<SqlField> fields = tableNameToFields.get(fqTableName);

			if (pkFieldNames == null)
			{
				pkFieldNames = new ArrayList<String>(0); // Dummy empty list
			}
			SqlField[] pkFields = new SqlField[pkFieldNames.size()];
			handleTechnicalFields(table, pkFieldNames, fields, pkFields);

			if (pkFields.length > 1 && log.isWarnEnabled())
			{
				PersistenceWarnUtil.logWarnOnce(log, loggerHistory, connection, "Table '" + table.getName()
						+ "' has a composite primary key which is currently not supported.");
			}
			if (pkFields.length > 0)
			{
				table.setIdFields(pkFields);
				pkFields[0].setIdIndex(ObjRef.PRIMARY_KEY_INDEX);
			}

			ILinkedMap<String, String[]> uniqueNameToFieldsMap = getUniqueConstraints(table);
			Collections.sort(fields, namedItemComparator);

			for (int a = 0, size = fields.size(); a < size; a++)
			{
				SqlField field = fields.get(a);
				field.setTable(table);
				table.mapField(field);
			}
			List<IField> alternateIdFields = new ArrayList<IField>();
			for (Entry<String, String[]> entry : uniqueNameToFieldsMap)
			{
				String[] columnNames = entry.getValue();
				if (columnNames.length != 1)
				{
					continue;
				}
				String fkFieldsKey = tableAndFieldToFKKey(fqTableName, columnNames[0]);
				if (fkFields.contains(fkFieldsKey))
				{
					continue;
				}
				// Single column unique constraints can be handled as alternate keys...
				String columnName = columnNames[0];
				IField uniqueConstraintField = table.getFieldByName(columnName);
				((Field) uniqueConstraintField).setAlternateId();
				((Field) uniqueConstraintField).setIdIndex((byte) alternateIdFields.size());
				alternateIdFields.add(uniqueConstraintField);
			}
			table.setAlternateIdFields(alternateIdFields.toArray(new IField[alternateIdFields.size()]));

			getTables().add(table);

			putTableByName(fqTableName, table);
		}
		findAndAssignFulltextFields();

		for (Entry<String, List<String[]>> entry : linkNameToEntryMap)
		{
			String linkName = entry.getKey();
			List<String[]> values = entry.getValue();
			List<SqlField> fields = tableNameToFields.get(linkName);
			ITable table = nameToTableDict.get(linkName);

			int hasPermissionGroupField = fieldsContainPermissionGroup(fields);

			if (table != null)
			{
				handleLinkWithinDataTable(linkName, values, fields);
			}
			else if (((fields.size() - hasPermissionGroupField) == 3) && values.size() == 2)
			{
				handleLinkTable(linkName, values);
			}
			else if (((fields.size() - hasPermissionGroupField) == 3) && values.size() == 1)
			{
				handleLinkTableToExtern(linkName, values);
			}
			else
			{
				throw new IllegalStateException("Type of link can not be determined: '" + linkName + "'");
			}
		}

		List<ITable> tables = getTables();
		for (int a = tables.size(); a-- > 0;)
		{
			ITable table = tables.get(a);
			table = serviceContext.registerWithLifecycle(table).finish();
			tables.set(a, table);
		}
		List<ILink> links = getLinks();
		for (int a = links.size(); a-- > 0;)
		{
			ILink link = links.get(a);
			link = serviceContext.registerWithLifecycle(link).finish();
			links.set(a, link);
		}
	}

	@Override
	public void afterStarted()
	{
		IList<IDatabaseMapper> objects = serviceContext.getObjects(IDatabaseMapper.class);
		for (int a = objects.size(); a-- > 0;)
		{
			objects.get(a).mapFields(this);
		}
		for (int a = objects.size(); a-- > 0;)
		{
			objects.get(a).mapLinks(this);
		}
		for (ITable table : getTables())
		{
			ITable permissionGroupTable = getTableByName(ormPatternMatcher.buildPermissionGroupFromTableName(table.getName()));
			if (permissionGroupTable != null)
			{
				mapPermissionGroupTable(permissionGroupTable, table);
			}
		}
		super.afterStarted();

		IList<IDatabaseMappedListener> databaseMappedListeners = serviceContext.getObjects(IDatabaseMappedListener.class);
		for (int a = databaseMappedListeners.size(); a-- > 0;)
		{
			databaseMappedListeners.get(a).databaseMapped(this);
		}
	}

	public void setCachedSchemaInfos(Map<String, Object> cachedSchemaInfos)
	{
		this.cachedSchemaInfos = cachedSchemaInfos;
	}

	public Map<String, Object> getCachedSchemaInfos()
	{
		return cachedSchemaInfos;
	}

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	public void setSchemaName(String schemaName)
	{
		this.schemaName = schemaName;
	}

	@Property(name = PersistenceConfigurationConstants.DatabaseTableIgnore, mandatory = false)
	public void setIgnoredTables(String ignoredTables)
	{
		String[] splitTableNames = ignoredTables.split("[:;]");
		this.ignoredTables.clear();
		this.ignoredTables.addAll(Arrays.asList(splitTableNames));
	}

	public void setDefaultCreatedByFieldName(String defaultCreatedByFieldName)
	{
		this.defaultCreatedByFieldName = defaultCreatedByFieldName;
	}

	public void setDefaultCreatedOnFieldName(String defaultCreatedOnFieldName)
	{
		this.defaultCreatedOnFieldName = defaultCreatedOnFieldName;
	}

	public void setDefaultUpdatedByFieldName(String defaultUpdatedByFieldName)
	{
		this.defaultUpdatedByFieldName = defaultUpdatedByFieldName;
	}

	public void setDefaultUpdatedOnFieldName(String defaultUpdatedOnFieldName)
	{
		this.defaultUpdatedOnFieldName = defaultUpdatedOnFieldName;
	}

	public void setDefaultVersionFieldName(String defaultVersionFieldName)
	{
		this.defaultVersionFieldName = defaultVersionFieldName;
	}

	@Property(name = PersistenceConfigurationConstants.LinkClass, mandatory = false)
	public void setLinkType(Class<? extends SqlLink> linkType)
	{
		this.linkType = linkType;
	}

	public void setJdbcDatabase(Connection jdbcDatabase)
	{
		connection = jdbcDatabase;
	}

	@Override
	public String[] getSchemaNames()
	{
		return schemaNames;
	}

	protected String tableAndFieldToFKKey(String tableName, String fieldName)
	{
		return tableName + "'''" + fieldName; // ''' will never ever come up in a sql name
	}

	protected void registerSqlKeywords() throws SQLException
	{
		String key = "sqlKeywords";
		String[] keywords = (String[]) cachedSchemaInfos.get(key);
		if (keywords == null)
		{
			keywords = connection.getMetaData().getSQLKeywords().split(",");
			cachedSchemaInfos.put(key, keywords);
		}
		for (int a = keywords.length; a-- > 0;)
		{
			sqlKeywordRegistry.registerSqlKeyword(keywords[a].trim());
		}
	}

	protected void loadLinkInfos(Map<String, List<String[]>> linkNameToEntryMap, Set<String> fkFields) throws SQLException
	{
		String key = "allForeignKeys";
		@SuppressWarnings("unchecked")
		IList<IMap<String, String>> allForeignKeys = (IList<IMap<String, String>>) cachedSchemaInfos.get(key);
		if (allForeignKeys == null)
		{
			allForeignKeys = new de.osthus.ambeth.collections.ArrayList<IMap<String, String>>();
			for (String schemaName : schemaNames)
			{
				IList<IMap<String, String>> schemasForeignKeys = connectionDialect.getExportedKeys(connection, schemaName);
				allForeignKeys.addAll(schemasForeignKeys);
			}
			cachedSchemaInfos.put(key, allForeignKeys);
		}

		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try
		{
			for (int i = allForeignKeys.size(); i-- > 0;)
			{
				IMap<String, String> foreignKey = allForeignKeys.get(i);
				try
				{
					String schemaName = foreignKey.get("OWNER");
					String constraintName = foreignKey.get("CONSTRAINT_NAME");
					String pkTableName = foreignKey.get("PKTABLE_NAME");
					String pkColumnName = foreignKey.get("PKCOLUMN_NAME");
					String linkTableName = foreignKey.get("FKTABLE_NAME");
					String linkColumnName = foreignKey.get("FKCOLUMN_NAME");

					sb.setLength(0);
					pkTableName = sb.append(schemaName).append(".").append(pkTableName).toString();
					sb.setLength(0);
					linkTableName = sb.append(schemaName).append(".").append(linkTableName).toString();

					List<String[]> tableLinks = linkNameToEntryMap.get(linkTableName);
					if (tableLinks == null)
					{
						tableLinks = new ArrayList<String[]>();
						linkNameToEntryMap.put(linkTableName, tableLinks);
					}
					tableLinks.add(new String[] { pkTableName, pkColumnName, linkTableName, linkColumnName, constraintName });
					fkFields.add(tableAndFieldToFKKey(linkTableName, linkColumnName));
				}
				finally
				{
					// foreignKey.dispose();
				}
			}
		}
		finally
		{
			// allForeignKeys.dispose();
			objectCollector.dispose(sb);
		}
	}

	@SuppressWarnings("unchecked")
	protected void loadTableNames(Set<String> fqTableNames) throws SQLException
	{
		String tableNamesKey = "tableNames";
		String viewNamesKey = "viewNames";
		Set<String> cachedTableNames = (Set<String>) cachedSchemaInfos.get(tableNamesKey);
		if (cachedTableNames == null)
		{
			IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
			StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
			try
			{
				List<String> fqTableNamesList = connectionDialect.getAllFullqualifiedTableNames(connection, schemaNames);
				List<String> fqViewNamesList = connectionDialect.getAllFullqualifiedViews(connection, schemaNames);
				for (String fqTableName : fqTableNamesList)
				{
					if (ignoredTables.contains(fqTableName))
					{
						continue; // this is not a table we are interested in
					}
					fqTableNames.add(fqTableName);
				}
				for (String fqViewName : fqViewNamesList)
				{
					if (ignoredTables.contains(fqViewName))
					{
						continue; // this is not a table we are interested in
					}
					fqTableNames.add(fqViewName);
					viewNames.add(fqViewName);
				}
			}
			finally
			{
				tlObjectCollector.dispose(sb);
			}

			cachedTableNames = new HashSet<String>(fqTableNames);
			cachedSchemaInfos.put(tableNamesKey, cachedTableNames);
			Set<String> cachedViewNames = new HashSet<String>(viewNames);
			cachedSchemaInfos.put(viewNamesKey, cachedViewNames);
		}
		else
		{
			fqTableNames.addAll(cachedTableNames);
			Set<String> cachedViewNames = (Set<String>) cachedSchemaInfos.get(viewNamesKey);
			viewNames.addAll(cachedViewNames);
		}
	}

	protected String[] getSchemaAndTableName(String tableName)
	{
		Matcher matcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(tableName);
		if (!matcher.matches())
		{
			throw new IllegalStateException("Must never happen");
		}
		return new String[] { matcher.group(1), matcher.group(2) };
	}

	protected void loadTableFields(Set<String> tableNames, Map<String, List<SqlField>> tableNameToFields) throws SQLException
	{
		for (String tableName : tableNames)
		{
			List<SqlField> fields = getFields(tableName);
			tableNameToFields.put(tableName, fields);
		}
	}

	protected List<SqlField> getFields(String tableName) throws SQLException
	{
		ILinkedMap<String, ColumnEntry> cachedFieldValues = getCachedFieldValues(tableName);
		ArrayList<SqlField> fields = new ArrayList<SqlField>(cachedFieldValues.size());
		for (Entry<String, ColumnEntry> entry : cachedFieldValues)
		{
			ColumnEntry columnEntry = entry.getValue();
			Class<?> javaType = JdbcUtil.getJavaTypeFromJdbcType(columnEntry.getJdbcTypeIndex(), columnEntry.getSize(), columnEntry.getDigits());
			Class<?> fieldSubType = connectionDialect.getComponentTypeByFieldTypeName(columnEntry.getTypeName());

			SqlField field = new SqlField();
			field.setName(columnEntry.getFieldName());
			field.setFieldType(javaType);
			field.setFieldSubType(fieldSubType);
			field.setConnection(sqlConnection);
			field.setSqlBuilder(sqlBuilder);
			field.setObjectCollector(objectCollector);
			field.setConversionHelper(conversionHelper);

			fields.add(field);
		}
		return fields;
	}

	@SuppressWarnings("unchecked")
	protected ILinkedMap<String, ColumnEntry> getCachedFieldValues(String tableName) throws SQLException
	{
		String key = "tableNameToFieldNamesToFieldValues";
		Map<String, ILinkedMap<String, ColumnEntry>> cachedTableNameToFieldValues = (Map<String, ILinkedMap<String, ColumnEntry>>) cachedSchemaInfos.get(key);
		if (cachedTableNameToFieldValues == null)
		{
			cachedTableNameToFieldValues = new HashMap<String, ILinkedMap<String, ColumnEntry>>();
			cachedSchemaInfos.put(key, cachedTableNameToFieldValues);
		}

		ILinkedMap<String, ColumnEntry> cachedFieldValues = cachedTableNameToFieldValues.get(tableName);
		if (cachedFieldValues == null)
		{
			cachedFieldValues = new LinkedHashMap<String, ColumnEntry>();
			cachedTableNameToFieldValues.put(tableName, cachedFieldValues);

			ColumnEntry rowIdEntry = new ColumnEntry("ROWID", -1, Types.ROWID, null, false, 0, 0, 0);
			cachedFieldValues.put(rowIdEntry.getFieldName(), rowIdEntry);

			String[] names = getSchemaAndTableName(tableName);
			ResultSet tableColumnsRS = connection.getMetaData().getColumns(null, names[0], names[1], null);
			try
			{
				while (tableColumnsRS.next())
				{
					String fieldName = tableColumnsRS.getString("COLUMN_NAME");
					int columnIndex = tableColumnsRS.getInt("ORDINAL_POSITION");
					int typeIndex = tableColumnsRS.getInt("DATA_TYPE");

					String typeName = tableColumnsRS.getString("TYPE_NAME");

					String isNullable = tableColumnsRS.getString("IS_NULLABLE");
					boolean nullable = "YES".equalsIgnoreCase(isNullable);

					int scale = tableColumnsRS.getInt("COLUMN_SIZE");
					int digits = tableColumnsRS.getInt("DECIMAL_DIGITS");
					int radix = tableColumnsRS.getInt("NUM_PREC_RADIX");

					ColumnEntry entry = new ColumnEntry(fieldName, columnIndex, typeIndex, typeName, nullable, scale, digits, radix);
					cachedFieldValues.put(entry.getFieldName(), entry);
				}
			}
			finally
			{
				JdbcUtil.close(tableColumnsRS);
				tableColumnsRS = null;
			}
		}

		return cachedFieldValues;
	}

	protected void grepDataTables(Set<String> fqTableNames, Set<String> fqDataTableNames, Map<String, List<SqlField>> tableNameToFields,
			Map<String, List<String[]>> linkNameToEntryMap) throws SQLException
	{
		for (String fqTableName : fqTableNames)
		{
			List<SqlField> fields = tableNameToFields.get(fqTableName);
			if (isLinkTable(fqTableName, fields, linkNameToEntryMap) || isLinkTableToExtern(fqTableName, fields, linkNameToEntryMap)
					|| isLinkArchiveTable(fqTableName, fields))
			{
				// This is a pure link table
				continue;
			}
			fqDataTableNames.add(fqTableName);
		}
	}

	@SuppressWarnings("unchecked")
	protected void mapPrimaryKeys(Set<String> tableNames, Map<String, List<String>> tableNameToPkFieldsMap) throws SQLException
	{
		String key = "tableNameToPkFieldsMap";
		Map<String, List<String>> cachedTableNameToPkFieldsMap = (Map<String, List<String>>) cachedSchemaInfos.get(key);
		if (cachedTableNameToPkFieldsMap == null)
		{
			Iterator<String> iter = tableNames.iterator();
			while (iter.hasNext())
			{
				String tableName = iter.next();
				String[] names = getSchemaAndTableName(tableName);
				ResultSet allPrimaryKeysRS = connection.getMetaData().getPrimaryKeys(null, names[0], names[1]);
				try
				{
					while (allPrimaryKeysRS.next())
					{
						String columnName = allPrimaryKeysRS.getString("COLUMN_NAME");
						short keySeq = allPrimaryKeysRS.getShort("KEY_SEQ");

						List<String> tableList = tableNameToPkFieldsMap.get(tableName);
						if (tableList == null)
						{
							tableList = new ArrayList<String>();
							tableNameToPkFieldsMap.put(tableName, tableList);
						}
						while (tableList.size() < keySeq)
						{
							tableList.add(null);
						}
						tableList.set(keySeq - 1, columnName);
					}
				}
				finally
				{
					JdbcUtil.close(allPrimaryKeysRS);
				}
			}
			cachedTableNameToPkFieldsMap = new HashMap<String, List<String>>(tableNameToPkFieldsMap);
			cachedSchemaInfos.put(key, cachedTableNameToPkFieldsMap);
		}
		else
		{
			tableNameToPkFieldsMap.putAll(cachedTableNameToPkFieldsMap);
		}
	}

	protected void handleTechnicalFields(final JdbcTable table, final List<String> pkFieldNames, final List<SqlField> fields, final SqlField[] pkFields)
	{
		for (int a = fields.size(); a-- > 0;)
		{
			SqlField field = fields.get(a);
			field.setTable(table);
			String fieldName = field.getName();
			if (fieldName.equalsIgnoreCase(defaultVersionFieldName))
			{
				table.setVersionField(field);
				fields.remove(a);
				continue;
			}
			else if (fieldName.equalsIgnoreCase(defaultCreatedByFieldName))
			{
				table.setCreatedByField(field);
				// fields.remove(a);
				continue;
			}
			else if (fieldName.equalsIgnoreCase(defaultCreatedOnFieldName))
			{
				table.setCreatedOnField(field);
				// fields.remove(a);
				continue;
			}
			else if (fieldName.equalsIgnoreCase(defaultUpdatedByFieldName))
			{
				table.setUpdatedByField(field);
				// fields.remove(a);
				continue;
			}
			else if (fieldName.equalsIgnoreCase(defaultUpdatedOnFieldName))
			{
				table.setUpdatedOnField(field);
				// fields.remove(a);
				continue;
			}
			else if (fieldName.equalsIgnoreCase("rowid"))
			{
				continue;
			}
			for (int b = pkFieldNames.size(); b-- > 0;)
			{
				if (pkFieldNames.get(b).equalsIgnoreCase(fieldName))
				{
					pkFields[b] = field;
					fields.remove(a);
					break;
				}
			}
		}
	}

	protected int fieldsContainPermissionGroup(List<SqlField> fields)
	{
		int permissionGroupCount = 0;
		for (SqlField field : fields)
		{
			if (PermissionGroup.permGroupIdNameOfData.equals(field.getName()))
			{
				permissionGroupCount++;
			}
			Matcher matcher = PermissionGroup.permGroupFieldForLink.matcher(field.getName());
			if (matcher.matches())
			{
				permissionGroupCount++;
			}
		}
		return permissionGroupCount;
	}

	protected boolean isLinkTable(String tableName, List<SqlField> fields, Map<String, List<String[]>> linkNameToEntryMap)
	{
		if (!linkNameToEntryMap.containsKey(tableName) || linkNameToEntryMap.get(tableName).size() != 2)
		{
			return false;
		}
		int hasPermissionGroupField = fieldsContainPermissionGroup(fields);
		return ((fields.size() - hasPermissionGroupField) == 3);
	}

	protected boolean isLinkTableToExtern(String tableName, List<SqlField> fields, Map<String, List<String[]>> linkNameToEntryMap) throws SQLException
	{
		return linkNameToEntryMap.containsKey(tableName) && fields.size() == 3 && linkNameToEntryMap.get(tableName).size() == 1
				&& getPrimaryKeyCount(tableName) == 2;
	}

	protected boolean isLinkArchiveTable(String tableName, List<SqlField> fields) throws SQLException
	{
		if (fields.size() != 4)
		{
			return false;
		}
		String[] archiveFieldNames = { "ARCHIVED_ON", "ARCHIVED_BY" };
		boolean archiveFieldNamesFound = false;
		int toFind = archiveFieldNames.length;
		for (int i = fields.size(); i-- > 0;)
		{
			String fieldName = fields.get(i).getName();
			if (fieldName.equals(archiveFieldNames[0]) || fieldName.equals(archiveFieldNames[1]))
			{
				toFind--;
			}
			if (toFind == 0)
			{
				archiveFieldNamesFound = true;
				break;
			}
		}
		// TODO check: Primarykey aus 3 Spalten, eine davon heißt ARCHIVED_ON, die anderen beiden nicht ARCHIVED_BY
		if (archiveFieldNamesFound && getPrimaryKeyCount(tableName) == 3)
		{
			linkArchiveTables.add(tableName);
		}
		return archiveFieldNamesFound;
	}

	@SuppressWarnings("unchecked")
	protected int getPrimaryKeyCount(String tableName) throws SQLException
	{
		String key = "primaryKeyCounts";
		Map<String, Integer> primaryKeyCounts = (Map<String, Integer>) cachedSchemaInfos.get(key);
		if (primaryKeyCounts == null)
		{
			primaryKeyCounts = new HashMap<String, Integer>();
			cachedSchemaInfos.put(key, primaryKeyCounts);
		}
		Integer count = primaryKeyCounts.get(tableName);
		if (count == null)
		{
			String[] names = getSchemaAndTableName(tableName);
			ResultSet allPrimaryKeysRS = connection.getMetaData().getPrimaryKeys(null, names[0], names[1]);
			count = 0;
			try
			{
				while (allPrimaryKeysRS.next())
				{
					count++;
				}
			}
			finally
			{
				JdbcUtil.close(allPrimaryKeysRS);
			}
			primaryKeyCounts.put(tableName, count);
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	protected void findAndAssignFulltextFields() throws SQLException
	{
		String key = "schemaToFulltextIndizes";
		Map<String, ILinkedMap<String, IList<String>>> schemaToFulltextIndizes = (Map<String, ILinkedMap<String, IList<String>>>) cachedSchemaInfos.get(key);
		if (schemaToFulltextIndizes == null)
		{
			schemaToFulltextIndizes = new HashMap<String, ILinkedMap<String, IList<String>>>();
			cachedSchemaInfos.put(key, schemaToFulltextIndizes);
		}

		for (String schemaName : schemaNames)
		{
			ILinkedMap<String, IList<String>> fulltextIndizes = schemaToFulltextIndizes.get(schemaName);
			if (fulltextIndizes == null)
			{
				fulltextIndizes = connectionDialect.getFulltextIndexes(connection, schemaName);
				schemaToFulltextIndizes.put(schemaName, fulltextIndizes);
			}

			for (Entry<String, IList<String>> entry : fulltextIndizes)
			{
				String tableName;
				tableName = entry.getKey();
				if (!singleSchema)
				{
					tableName = schemaName + "." + tableName;
				}
				ITable table = nameToTableDict.get(tableName);
				IList<String> fieldNames = entry.getValue();
				if (table != null && table instanceof Table)
				{
					((Table) table).setFulltextFieldsByNames(fieldNames);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected ILinkedMap<String, String[]> getUniqueConstraints(ITable table) throws SQLException
	{
		String key = "tableToUniqueConstraints";
		Map<String, LinkedHashMap<String, String[]>> tableToUniqueConstraints = (Map<String, LinkedHashMap<String, String[]>>) cachedSchemaInfos.get(key);
		if (tableToUniqueConstraints == null)
		{
			tableToUniqueConstraints = new HashMap<String, LinkedHashMap<String, String[]>>();
			cachedSchemaInfos.put(key, tableToUniqueConstraints);
		}

		LinkedHashMap<String, String[]> uniqueNameToFieldsMap = tableToUniqueConstraints.get(table.getName());
		if (uniqueNameToFieldsMap == null)
		{
			String[] names = getSchemaAndTableName(table.getName());
			ResultSet allUniqueKeysRS = connectionDialect.getIndexInfo(connection, names[0], names[1], true);
			try
			{
				uniqueNameToFieldsMap = new LinkedHashMap<String, String[]>();
				IField idField = table.getIdField();
				while (allUniqueKeysRS.next())
				{
					String indexName = allUniqueKeysRS.getString("INDEX_NAME");
					if (indexName == null)
					{
						continue;
					}
					// String tableName = allUniqueKeysRS.getString("TABLE_NAME");
					// boolean nonUnique = allUniqueKeysRS.getBoolean("NON_UNIQUE");
					// String indexQualifier = allUniqueKeysRS.getString("INDEX_QUALIFIER");
					// short type = allUniqueKeysRS.getShort("TYPE");
					short ordinalPosition = allUniqueKeysRS.getShort("ORDINAL_POSITION");
					String columnName = allUniqueKeysRS.getString("COLUMN_NAME");
					if (idField != null && idField.getName().equals(columnName))
					{
						continue;
					}
					// String ascOrDesc = allUniqueKeysRS.getString("ASC_OR_DESC");
					// int cardinality = allUniqueKeysRS.getInt("CARDINALITY");

					String[] columnNames = uniqueNameToFieldsMap.get(indexName);
					if (columnNames == null)
					{
						columnNames = new String[ordinalPosition];
						uniqueNameToFieldsMap.put(indexName, columnNames);
					}
					if (columnNames.length < ordinalPosition)
					{
						String[] newColumnNames = new String[ordinalPosition];
						System.arraycopy(columnNames, 0, newColumnNames, 0, columnNames.length);
						columnNames = newColumnNames;
						uniqueNameToFieldsMap.put(indexName, columnNames);
					}
					columnNames[ordinalPosition - 1] = columnName;
				}

				tableToUniqueConstraints.put(table.getName(), uniqueNameToFieldsMap);
			}
			finally
			{
				JdbcUtil.close(allUniqueKeysRS);
				allUniqueKeysRS = null;
			}
		}

		return uniqueNameToFieldsMap;
	}

	protected SqlLink buildAndMapLink(String linkName, ITable fromTable, IField fromField, ITable toTable, IField toField, boolean nullable)
	{
		SqlLink link;
		try
		{
			link = linkType.newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		link.setName(linkName);
		link.setFromTable(fromTable);
		link.setFromField(fromField);
		link.setToTable(toTable);
		link.setToField(toField);
		link.setNullable(nullable);

		DirectedLink directedLink = new DirectedLink();
		directedLink.setLink(link);
		directedLink.setFromTable(fromTable);
		directedLink.setFromField(fromField);
		directedLink.setToTable(toTable);
		directedLink.setToField(toField);
		directedLink.setObjectCollector(objectCollector);
		directedLink.setReverse(false);
		directedLink.afterPropertiesSet();

		DirectedLink revDirectedLink = new DirectedLink();
		revDirectedLink.setLink(link);
		revDirectedLink.setFromTable(toTable);
		revDirectedLink.setFromField(toField);
		revDirectedLink.setToTable(fromTable);
		revDirectedLink.setToField(fromField);
		revDirectedLink.setObjectCollector(objectCollector);
		revDirectedLink.setReverse(true);
		revDirectedLink.afterPropertiesSet();

		link.setDirectedLink(directedLink);
		link.setReverseDirectedLink(revDirectedLink);

		link = (SqlLink) mapLink(link);

		return link;
	}

	@Override
	public ILink mapLink(ILink link)
	{
		putLinkByName(link.getName(), link);
		links.add(link);

		Table fromTable = (Table) link.getFromTable();
		Table toTable = (Table) link.getToTable();

		fromTable.mapLink(link.getDirectedLink());
		if (toTable != null)
		{
			toTable.mapLink(link.getReverseDirectedLink());
		}

		link = serviceContext.registerWithLifecycle(link).finish();

		return link;
	}

	protected void handleLinkTable(String linkName, List<String[]> values) throws SQLException
	{
		// Pure link table
		String pkTableName = values.get(0)[0];
		String pkFieldName = values.get(0)[1];
		String linkColumnName = values.get(0)[3];
		String constraintName = values.get(0)[4];
		String pkTableName2 = values.get(1)[0];
		String pkFieldName2 = values.get(1)[1];
		String linkColumnName2 = values.get(1)[3];
		String constraintName2 = values.get(1)[4];

		ITable fromTable = null, toTable = null;
		IField fromField = null, toField = null;
		String fromConstraint = null, toConstraint = null;

		ITable entityTable1 = nameToTableDict.get(pkTableName);
		ITable entityTable2 = nameToTableDict.get(pkTableName2);
		IField entityIdField1 = entityTable1.getFieldByName(pkFieldName);
		IField entityIdField2 = entityTable2.getFieldByName(pkFieldName2);

		ILinkedMap<String, ColumnEntry> cachedFieldValues = getCachedFieldValues(linkName);
		for (Entry<String, ColumnEntry> entry2 : cachedFieldValues)
		{
			ColumnEntry entry = entry2.getValue();
			String fieldName = entry.getFieldName();
			int columnIndex = entry.getColumnIndex();
			int typeIndex = entry.getJdbcTypeIndex();
			int scale = entry.getSize();
			int digits = entry.getDigits();

			IBeanRuntime<SqlField> field = serviceContext.registerBean(SqlField.class);
			field.propertyValue("Name", fieldName);
			field.propertyValue("FieldType", JdbcUtil.getJavaTypeFromJdbcType(typeIndex, scale, digits));

			if (fieldName.equals(linkColumnName))
			{
				if (columnIndex == 1) // "from" column
				{
					fromTable = entityTable1;
					field.propertyValue("Table", fromTable);
					field.propertyValue("IdIndex", entityIdField1.getIdIndex());
					fromField = field.finish();
					fromConstraint = constraintName;
				}
				else if (columnIndex == 2)// "to" column
				{
					toTable = entityTable1;
					field.propertyValue("Table", toTable);
					field.propertyValue("IdIndex", entityIdField1.getIdIndex());
					toField = field.finish();
					toConstraint = constraintName;
				}
			}
			else if (fieldName.equals(linkColumnName2))
			{
				if (columnIndex == 1) // "from" column
				{
					fromTable = entityTable2;
					field.propertyValue("Table", fromTable);
					field.propertyValue("IdIndex", entityIdField2.getIdIndex());
					fromField = field.finish();
					fromConstraint = constraintName2;
				}
				else if (columnIndex == 2)// "to" column
				{
					toTable = entityTable2;
					field.propertyValue("Table", toTable);
					field.propertyValue("IdIndex", entityIdField2.getIdIndex());
					toField = field.finish();
					toConstraint = constraintName2;
				}
			}
		}

		SqlLink link = buildAndMapLink(linkName, fromTable, fromField, toTable, toField, true);
		link.setFullqualifiedEscapedTableName(XmlDatabaseMapper.escapeName(linkName));
		((DirectedLink) link.getDirectedLink()).setConstraintName(fromConstraint);
		((DirectedLink) link.getDirectedLink()).setStandaloneLink(true);
		((DirectedLink) link.getReverseDirectedLink()).setStandaloneLink(true);
		((DirectedLink) link.getReverseDirectedLink()).setConstraintName(toConstraint);

		link = serviceContext.registerWithLifecycle(link).finish();

		putLinkByDefiningName(link.getName(), link);
		addLinkByTables(link);

		if (log.isDebugEnabled())
		{
			PersistenceWarnUtil.logDebugOnce(log, loggerHistory, connection,
					"Recognizing table '" + link.getName() + "' as link between table '" + fromTable.getName() + "' and '" + toTable.getName() + "'");
		}
	}

	protected void putLinkByDefiningName(String name, ILink link)
	{
		putObjectByName(name, link, definingNameToLinkDict);
	}

	protected void putLinkByName(String name, ILink link)
	{
		putObjectByName(name, link, nameToLinkDict);
	}

	protected void putTableByName(String name, ITable table)
	{
		putObjectByName(name, table, nameToTableDict);
	}

	protected <T> void putObjectByName(String name, T link, IMap<String, T> nameToObjectMap)
	{
		String[] schemaAndName = XmlDatabaseMapper.splitSchemaAndName(name);
		String schemaName = schemaAndName[0];
		String softName = schemaAndName[1];
		if (schemaName == null)
		{
			schemaName = schemaNames[0];
		}
		if (schemaName.equals(schemaNames[0]))
		{
			nameToObjectMap.put(softName, link);
		}
		nameToObjectMap.put(schemaName + "." + softName, link);
	}

	protected void handleLinkTableToExtern(String linkName, List<String[]> values) throws SQLException
	{
		// Pure link table to external entity
		String[] onlyValue = values.get(0);
		String entityTableName = onlyValue[0];
		String entityIdFieldName = onlyValue[1];
		// String linkTableName = onlyValue[2];
		String linkColumnName = onlyValue[3];

		ITable fromTable = null;
		IField fromField = null, toField = null;
		ITable entityTable = nameToTableDict.get(entityTableName);
		IField entityIdField = entityTable.getFieldByName(entityIdFieldName);

		ILinkedMap<String, ColumnEntry> cachedFieldValues = getCachedFieldValues(linkName);
		for (Entry<String, ColumnEntry> entry2 : cachedFieldValues)
		{
			ColumnEntry entry = entry2.getValue();
			String fieldName = entry.getFieldName();
			int typeIndex = entry.getJdbcTypeIndex();
			int scale = entry.getSize();
			int digits = entry.getDigits();

			IBeanRuntime<SqlField> field = serviceContext.registerBean(SqlField.class).propertyValue("Name", fieldName)
					.propertyValue("FieldType", JdbcUtil.getJavaTypeFromJdbcType(typeIndex, scale, digits));

			if (fieldName.equals(linkColumnName))
			{
				fromTable = entityTable;
				field.propertyValue("Table", fromTable);
				field.propertyValue("IdIndex", entityIdField.getIdIndex());
				fromField = field.finish();
			}
			else
			{
				field.propertyValue("Table", new Table());
				toField = field.finish();
			}
		}

		SqlLink link;
		try
		{
			link = linkType.newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		link.setName(linkName);
		link.setFullqualifiedEscapedTableName(XmlDatabaseMapper.escapeName(linkName));
		link.setFromTable(fromTable);
		link.setFromField(fromField);
		link.setToField(toField);
		link.setNullable(true);

		DirectedExternalLink directedLink = new DirectedExternalLink();
		directedLink.setLink(link);
		directedLink.setFromTable(fromTable);
		directedLink.setFromField(fromField);
		directedLink.setToField(toField);
		directedLink.setStandaloneLink(true);
		directedLink.setReverse(false);
		directedLink.setObjectCollector(objectCollector);
		// afterPropertiesSet() and additional injections in XmlDatabaseMapper (later)

		DirectedExternalLink revDirectedLink = new DirectedExternalLink();
		revDirectedLink.setLink(link);
		revDirectedLink.setFromField(toField);
		revDirectedLink.setToTable(fromTable);
		revDirectedLink.setToField(fromField);
		revDirectedLink.setStandaloneLink(true);
		revDirectedLink.setReverse(true);
		revDirectedLink.setObjectCollector(objectCollector);
		// afterPropertiesSet() and additional injections in XmlDatabaseMapper (later)

		link.setDirectedLink(directedLink);
		link.setReverseDirectedLink(revDirectedLink);

		putLinkByName(link.getName(), link);
		putLinkByDefiningName(link.getName(), link);
		addLinkByTables(link);

		if (log.isDebugEnabled())
		{
			PersistenceWarnUtil.logDebugOnce(log, loggerHistory, connection, "Recognizing table '" + linkName + "' as link between table '" + values.get(0)[0]
					+ "' and an external entity");
		}
	}

	protected void handleLinkWithinDataTable(String linkName, List<String[]> values, List<SqlField> fields) throws SQLException
	{
		// Entity table with links
		for (int i = values.size(); i-- > 0;)
		{
			String entityTableName = values.get(i)[0];
			String entityColumnName = values.get(i)[1];
			String linkColumnName = values.get(i)[3];
			String constraintName = values.get(i)[4];

			ITable fromTable;
			IField fromField;
			ITable toTable;
			IField toField;

			fromTable = nameToTableDict.get(linkName);
			fromField = fromTable.getFieldByName(linkColumnName);
			toTable = nameToTableDict.get(entityTableName);
			toField = toTable.getFieldByName(entityColumnName);

			if (fromField == null)
			{
				fromField = fromTable.getIdField();
			}
			if (toField == null)
			{
				toField = toTable.getIdField();
			}
			if (fromField.getIdIndex() == ObjRef.UNDEFINED_KEY_INDEX)
			{
				((Field) fromField).setIdIndex(toField.getIdIndex());
			}
			if (!fromField.isAlternateId())
			{
				fromField.getTable().getPrimitiveFields().remove(fromField);
			}
			if (!toField.isAlternateId())
			{
				toField.getTable().getPrimitiveFields().remove(toField);
			}
			boolean nullable = isFieldNullable(fromField);

			SqlLink link = buildAndMapLink(createForeignKeyLinkName(fromTable.getName(), fromField.getName(), toTable.getName(), toField.getName()), fromTable,
					fromField, toTable, toField, nullable);
			link.setConstraintName(constraintName);
			link.setTableName(linkName);
			String[] schemaAndName = XmlDatabaseMapper.splitSchemaAndName(linkName);
			link.setFullqualifiedEscapedTableName(XmlDatabaseMapper.escapeName(schemaAndName[0], schemaAndName[1]));

			boolean fromIsStandalone = fromField.isAlternateId() || (fromTable.getIdField() != null && fromTable.getIdField().equals(fromField));
			boolean toIsStandalone = toField.isAlternateId() || toTable.getIdField().equals(toField);
			((DirectedLink) link.getDirectedLink()).setStandaloneLink(fromIsStandalone);
			((DirectedLink) link.getReverseDirectedLink()).setStandaloneLink(toIsStandalone);

			link = serviceContext.registerWithLifecycle(link).finish();

			putLinkByDefiningName(constraintName, link);
			addLinkByTables(link);

			if (log.isDebugEnabled())
			{
				PersistenceWarnUtil.logDebugOnce(log, loggerHistory, connection, "Recognizing table '" + fromTable.getName() + "' as data table with link ("
						+ link.getName() + ") to table '" + toTable.getName() + "'");
			}
		}
	}

	@Override
	public boolean isFieldNullable(IField field) throws SQLException
	{
		boolean nullable = false;

		String tableName = field.getTable().getName();
		ILinkedMap<String, ColumnEntry> cachedFieldValues = getCachedFieldValues(tableName);
		ColumnEntry entry = cachedFieldValues.get(field.getName());
		nullable = entry.isNullable();

		return nullable;
	}

	@Override
	public boolean isLinkArchiveTable(String tableName)
	{
		return linkArchiveTables.contains(tableName);
	}

	@Override
	public void flush()
	{
		try
		{
			connectionDialect.commit(connection);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void revert()
	{
		alreadyLinkedCache.clear();
		try
		{
			connectionDialect.rollback(connection);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void revert(ISavepoint savepoint)
	{
		alreadyLinkedCache.clear();
		try
		{
			rollback(savepoint);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean test()
	{
		if (System.currentTimeMillis() - lastTestTime <= trustTime)
		{
			return true;
		}
		try
		{
			try
			{
				return connection.isValid(0);
			}
			catch (AbstractMethodError e)
			{
				// Oracle driver does not support this operation
				return !connection.isClosed();
			}
		}
		catch (SQLException e)
		{
			return false;
		}
		finally
		{
			lastTestTime = System.currentTimeMillis();
		}
	}

	@Override
	public void dispose()
	{
		connection = null;
		super.dispose();
	}

	@Override
	public void registerDatabaseMappedListener(IDatabaseMappedListener databaseMappedListener)
	{
		listeners.register(databaseMappedListener);
	}

	@Override
	public void unregisterDatabaseMappedListener(IDatabaseMappedListener databaseMappedListener)
	{
		listeners.unregister(databaseMappedListener);
	}

	@Override
	public ISavepoint setSavepoint()
	{
		try
		{
			return new JdbcSavepoint(connection.setSavepoint());
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void releaseSavepoint(ISavepoint savepoint)
	{
		try
		{
			connectionDialect.releaseSavepoint(((JdbcSavepoint) savepoint).getSavepoint(), connection);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void rollback(ISavepoint savepoint)
	{
		try
		{
			connection.rollback(((JdbcSavepoint) savepoint).getSavepoint());
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IList<String[]> disableConstraints()
	{
		return connectionDialect.disableConstraints(connection);
	}

	@Override
	public void enableConstraints(IList<String[]> disabled)
	{
		connectionDialect.enableConstraints(connection, disabled);
	}
}
