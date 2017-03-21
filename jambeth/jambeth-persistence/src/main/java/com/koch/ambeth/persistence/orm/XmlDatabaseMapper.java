package com.koch.ambeth.persistence.orm;

/*-
 * #%L
 * jambeth-persistence
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityMetaDataExtendable;
import com.koch.ambeth.merge.config.IEntityMetaDataReader;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.merge.orm.CascadeDeleteDirection;
import com.koch.ambeth.merge.orm.CompositeMemberConfig;
import com.koch.ambeth.merge.orm.EntityIdentifier;
import com.koch.ambeth.merge.orm.ExternalLinkConfig;
import com.koch.ambeth.merge.orm.IEntityConfig;
import com.koch.ambeth.merge.orm.ILinkConfig;
import com.koch.ambeth.merge.orm.IMemberConfig;
import com.koch.ambeth.merge.orm.IOrmConfigGroup;
import com.koch.ambeth.merge.orm.IOrmConfigGroupProvider;
import com.koch.ambeth.merge.orm.IRelationConfig;
import com.koch.ambeth.merge.orm.MemberConfig;
import com.koch.ambeth.merge.orm.OrmConfigGroup;
import com.koch.ambeth.merge.orm.RelationConfig20;
import com.koch.ambeth.merge.orm.RelationConfigLegathy;
import com.koch.ambeth.merge.orm.blueprint.IOrmDatabaseMapper;
import com.koch.ambeth.persistence.DatabaseMetaData;
import com.koch.ambeth.persistence.DirectedExternalLinkMetaData;
import com.koch.ambeth.persistence.DirectedLinkMetaData;
import com.koch.ambeth.persistence.IConfigurableDatabaseMetaData;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.LinkMetaData;
import com.koch.ambeth.persistence.TableMetaData;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ILinkMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.database.IDatabaseMappedListener;
import com.koch.ambeth.persistence.sql.SqlLinkMetaData;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.StringConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptySet;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.ISetEntry;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class XmlDatabaseMapper extends DefaultDatabaseMapper implements IStartingBean, IDisposableBean, IOrmDatabaseMapper
{
	public static final Pattern fqToSoftTableNamePattern = Pattern.compile("[\"`]?(.+?)[\"`]?\\.[\"`]?(.+?)[\"`]?");

	public static final Pattern softTableNamePattern = Pattern.compile("[\"`]?(.+?)[\"`]?");

	public static String[] splitSchemaAndName(String fqName)
	{
		Matcher splitMatcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(fqName);
		String schemaName = null, softName = null;
		if (splitMatcher.matches())
		{
			schemaName = splitMatcher.group(1);
			softName = splitMatcher.group(2);
		}
		else
		{
			splitMatcher = XmlDatabaseMapper.softTableNamePattern.matcher(fqName);
			if (splitMatcher.matches())
			{
				// set "default" schema name
				softName = splitMatcher.group(1);
			}
			else
			{
				throw new IllegalArgumentException("Illegal full qualified name '" + fqName + "'");
			}
		}
		return new String[] { schemaName, softName };
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IEntityMetaDataExtendable entityMetaDataExtendable;

	@Autowired
	protected IEntityMetaDataReader entityMetaDataReader;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IOrmConfigGroupProvider ormConfigGroupProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired
	protected IServiceContext serviceContext;

	protected String xmlFileName = null;

	protected final List<EntityMetaData> registeredMetaDatas = new ArrayList<EntityMetaData>();

	protected IOrmConfigGroup ormConfigGroup = new OrmConfigGroup(EmptySet.<IEntityConfig> emptySet(), EmptySet.<IEntityConfig> emptySet());

	protected HashSet<String> allFullqualifiedSequences;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Override
	public void afterStarted() throws Throwable
	{
		if (xmlFileName != null)
		{
			ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(xmlFileName);
		}
	}

	@Override
	public void destroy() throws Throwable
	{
		for (int a = registeredMetaDatas.size(); a-- > 0;)
		{
			entityMetaDataExtendable.unregisterEntityMetaData(registeredMetaDatas.get(a));
		}
	}

	@Property(name = ServiceConfigurationConstants.mappingFile, mandatory = false)
	public void setFileName(String fileName)
	{
		if (xmlFileName != null)
		{
			throw new IllegalArgumentException("XmlDatabaseMapper already configured! Tried to set the config file '" + fileName
					+ "'. File name is already set to '" + xmlFileName + "'");
		}

		xmlFileName = fileName;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Property(name = ServiceConfigurationConstants.mappingResource, mandatory = false)
	public void setResourceName(String xmlResourceName)
	{
		if (xmlFileName != null)
		{
			throw new IllegalArgumentException("XmlDatabaseMapper already configured! Tried to set the config resource '" + xmlResourceName
					+ "'. Resource name is already set to '" + xmlFileName + "'");
		}

		xmlFileName = xmlResourceName;
	}

	@Override
	public void mapFields(Connection connection, String[] schemaNames, IDatabaseMetaData database)
	{
		try
		{
			allFullqualifiedSequences = new HashSet<String>(connectionDialect.getAllFullqualifiedSequences(connection, schemaNames))
			{
				@Override
				protected boolean equalKeys(String key, ISetEntry<String> entry)
				{
					return key.equalsIgnoreCase(entry.getKey());
				}

				@Override
				protected int extractHash(String key)
				{
					return key.toLowerCase().hashCode();
				}
			};
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (xmlFileName == null)
		{
			// No config source was set
			return;
		}

		handleExternalEntities();
		mapFieldsIntern(ormConfigGroup, database);
		super.mapFields(connection, schemaNames, database);
	}

	protected List<ITableMetaData> mapFieldsIntern(IOrmConfigGroup ormConfigGroup, IDatabaseMetaData database)
	{
		ArrayList<ITableMetaData> tables = new ArrayList<ITableMetaData>();
		int maxNameLength = database.getMaxNameLength();
		for (IEntityConfig entityConfig : ormConfigGroup.getLocalEntityConfigs())
		{
			Class<?> entityType = entityConfig.getEntityType();
			Class<?> realType = entityConfig.getRealType();
			String idName = this.idName;
			String versionName = this.versionName;

			ITableMetaData table = tryResolveTable(database, entityConfig.getTableName(), realType.getSimpleName());
			table = database.mapTable(table.getName(), entityType);

			ITableMetaData archiveTable = database.getTableByName(ormPatternMatcher.buildArchiveFromTableName(table.getName(), maxNameLength));
			if (archiveTable != null)
			{
				database.mapArchiveTable(archiveTable.getName(), entityType);
			}

			configureTableSequence(table, entityConfig.getSequenceName(), maxNameLength, allFullqualifiedSequences);

			ITableMetaData pgTable = database.getTableByName(ormPatternMatcher.buildPermissionGroupFromTableName(table.getName(), maxNameLength));
			if (pgTable != null)
			{
				configureTableSequence(pgTable, null, maxNameLength, allFullqualifiedSequences);
			}

			if (entityConfig.getIdMemberConfig() != null)
			{
				IMemberConfig idMemberConfig = entityConfig.getIdMemberConfig();
				idName = idMemberConfig.getName();
				if (idMemberConfig instanceof MemberConfig)
				{
					handleIdField(new MemberConfig[] { (MemberConfig) idMemberConfig }, table);
				}
				else
				{
					handleIdField(((CompositeMemberConfig) idMemberConfig).getMembers(), table);
				}
			}

			if (entityConfig.isVersionRequired() && entityConfig.getVersionMemberConfig() != null)
			{
				IMemberConfig versionMemberConfig = entityConfig.getVersionMemberConfig();
				versionName = versionMemberConfig.getName();
				handleVersionField(versionMemberConfig, table);
			}
			if (entityConfig.getDescriminatorName() != null)
			{
				handleDescriminatorField(entityConfig.getDescriminatorName(), table);
			}

			HashSet<String> ignoredMembers = new HashSet<String>();
			Iterable<IMemberConfig> memberIter = entityConfig.getMemberConfigIterable();
			for (IMemberConfig memberConfig : memberIter)
			{
				if (memberConfig.isTransient())
				{
					continue;
				}
				if (memberConfig instanceof CompositeMemberConfig)
				{
					MemberConfig[] members = ((CompositeMemberConfig) memberConfig).getMembers();
					for (MemberConfig member : members)
					{
						mapBasic(table, member);
					}
				}
				else
				{
					mapBasic(table, (MemberConfig) memberConfig);
				}
				if (memberConfig.isIgnore())
				{
					ignoredMembers.add(memberConfig.getName());
					continue;
				}
			}

			if (entityConfig.isVersionRequired() && table.getVersionField() == null)
			{
				throw new IllegalStateException("No version field found in table '" + table.getName() + "'");
			}

			IMap<String, IPropertyInfo> propertyMap = propertyInfoProvider.getPropertyMap(entityType);
			HashMap<String, IPropertyInfo> ucPropertyMap = new HashMap<String, IPropertyInfo>();
			for (Entry<String, IPropertyInfo> entry : propertyMap)
			{
				String memberName = entry.getKey();
				if (ignoredMembers.contains(memberName) || table.getFieldByMemberName(memberName) != null)
				{
					continue;
				}
				IPropertyInfo propertyInfo = entry.getValue();
				int modifiers = propertyInfo.getModifiers();
				if (Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers))
				{
					continue;
				}
				addToUcPropertyMap(memberName, propertyInfo, ucPropertyMap, maxNameLength);
			}
			List<IFieldMetaData> fields = table.getAllFields();
			ArrayList<IFieldMetaData> fieldsCopy = new ArrayList<IFieldMetaData>(fields);
			Collections.sort(fieldsCopy, new Comparator<IFieldMetaData>()
			{
				@Override
				public int compare(IFieldMetaData o1, IFieldMetaData o2)
				{
					return o2.getName().compareTo(o1.getName()); // Reverse order
				}
			});

			for (int a = fieldsCopy.size(); a-- > 0;)
			{
				IFieldMetaData field = fieldsCopy.get(a);
				if (field.getMember() != null)
				{
					// Field already mapped
					continue;
				}
				String fieldName = field.getName();
				IPropertyInfo propertyInfo = ucPropertyMap.get(fieldName);
				if (propertyInfo == null)
				{
					if (field.expectsMapping() && log.isDebugEnabled())
					{
						log.debug("No member specified or autoresolved on entity '" + entityType.getName() + "' for database field '" + table.getName() + "."
								+ field.getName() + "'");
					}
					continue;
				}
				if (log.isDebugEnabled())
				{
					log.debug("Autoresolving member '" + entityType.getName() + "." + propertyInfo.getName() + "' for database field '" + table.getName() + "."
							+ field.getName() + "'");
				}
				table.mapField(field.getName(), propertyInfo.getName());
			}
			mapIdAndVersion(table, idName, versionName);
			tables.add(table);
		}
		return tables;
	}

	protected void configureTableSequence(ITableMetaData table, String sequenceName, int maxNameLength, ISet<String> allFullqualifiedSequences)
	{
		if (sequenceName == null)
		{
			sequenceName = ormPatternMatcher.buildSequenceFromTableName(table.getName(), maxNameLength);
		}
		String fqSequenceName = getFqObjectName(table, sequenceName);
		fqSequenceName = allFullqualifiedSequences.get(fqSequenceName); // important: set the camelCase of the existing sequence in the database
		if (fqSequenceName == null)
		{
			fqSequenceName = sequenceName;
		}
		((TableMetaData) table).setSequenceName(fqSequenceName);
	}

	@Override
	public void mapLinks(Connection connection, String[] schemaNames, IDatabaseMetaData database)
	{
		if (xmlFileName == null)
		{
			// No config source was set
			return;
		}
		int maxNameLength = database.getMaxNameLength();
		IConfigurableDatabaseMetaData confDatabase = (IConfigurableDatabaseMetaData) database;
		List<ILinkMetaData> allLinks = database.getLinks();
		for (int i = allLinks.size(); i-- > 0;)
		{
			ILinkMetaData link = allLinks.get(i);
			if (link.getName().equalsIgnoreCase(link.getTableName()))
			{
				String archiveTableName = ormPatternMatcher.buildArchiveFromTableName(link.getName(), maxNameLength);
				if (confDatabase.isLinkArchiveTable(archiveTableName))
				{
					((LinkMetaData) link).setArchiveTableName(archiveTableName);
				}
			}
		}

		DatabaseMetaData databaseImpl = (DatabaseMetaData) database;
		for (IEntityConfig entityConfig : ormConfigGroup.getLocalEntityConfigs())
		{
			Class<?> entityType = entityConfig.getEntityType();

			ITableMetaData table = getTableByType(database, entityType);

			Iterable<IRelationConfig> relationIter = entityConfig.getRelationConfigIterable();
			for (IRelationConfig relationConfig : relationIter)
			{
				if (relationConfig instanceof RelationConfigLegathy)
				{
					RelationConfigLegathy relationConfigLegathy = (RelationConfigLegathy) relationConfig;
					if (relationConfigLegathy.isToOne())
					{
						mapToOne(connection, databaseImpl, table, relationConfigLegathy);
					}
					else
					{
						mapToMany(connection, databaseImpl, table, relationConfigLegathy);
					}
				}
				else if (relationConfig instanceof RelationConfig20)
				{
					RelationConfig20 relationConfig20 = (RelationConfig20) relationConfig;
					mapRelation(relationConfig20, table, databaseImpl, connection);
				}
				else
				{
					throw new RuntimeException("RelationConfig of type '" + relationConfig.getClass().getSimpleName() + "' not yet supported");
				}
			}
		}

		super.mapLinks(connection, schemaNames, database);
	}

	protected ITableMetaData tryResolveTable(IDatabaseMetaData database, String hardName, String softName)
	{
		if (hardName != null && hardName.length() > 0)
		{
			ITableMetaData table = database.getTableByName(connectionDialect.toDefaultCase(hardName));
			if (table == null)
			{
				throw new IllegalArgumentException("No table found with name '" + hardName + "'");
			}
			return table;
		}
		if (softName == null || softName.length() == 0)
		{
			throw new IllegalArgumentException("No table name specified");
		}
		int maxProcedureNameLength = database.getMaxNameLength();
		String assumedName = ormPatternMatcher.buildTableNameFromSoftName(softName, maxProcedureNameLength);

		ITableMetaData table = null;
		{
			ISet<String> producedNameCandidates = produceNameCandidates(assumedName);
			for (String name : producedNameCandidates)
			{
				table = database.getTableByName(name);
				if (table != null)
				{
					break;
				}
			}
			if (table == null)
			{
				List<ITableMetaData> tables = database.getTables();
				for (String name : producedNameCandidates)
				{
					for (ITableMetaData currTable : tables)
					{
						String[] schemaAndTableName = sqlBuilder.getSchemaAndTableName(currTable.getName());
						if (schemaAndTableName[1].equalsIgnoreCase(name))
						{
							table = currTable;
							break;
						}
					}
				}
			}
		}
		if (table == null)
		{
			throw new IllegalArgumentException("No table could be autoresolved with entity name '" + softName + "' in schema '" + database.getSchemaNames()[0]
					+ "'");
		}
		return table;
	}

	protected ISet<String> produceNameCandidates(String assumedName)
	{
		HashSet<String> set = new HashSet<String>();
		set.add(assumedName);
		set.add(assumedName.toUpperCase());
		set.add(assumedName.toLowerCase());

		assumedName = StringConversionHelper.insertUnderscoreBeforeUppercaseLetter(objectCollector, assumedName);
		set.add(assumedName);
		set.add(assumedName.toUpperCase());
		set.add(assumedName.toLowerCase());

		assumedName = StringConversionHelper.insertUnderscoreBeforeNumbers(objectCollector, assumedName);
		set.add(assumedName);
		set.add(assumedName.toUpperCase());
		set.add(assumedName.toLowerCase());
		return set;
	}

	protected void handleIdField(MemberConfig[] idMemberConfig, ITableMetaData table)
	{
		IFieldMetaData[] idFields = table.getIdFields();
		if (idFields != null)
		{
			return;
		}
		idFields = new IFieldMetaData[idMemberConfig.length];
		for (int a = 0, size = idMemberConfig.length; a < size; a++)
		{
			String idColumnName = idMemberConfig[a].getColumnName();
			if (idColumnName != null && !idColumnName.isEmpty() && table instanceof TableMetaData)
			{
				idFields[a] = table.getFieldByName(idColumnName);
			}
			else
			{
				throw new IllegalStateException("Cannot set id field");
			}
		}
		((TableMetaData) table).setIdFields(idFields);
	}

	protected void handleVersionField(IMemberConfig versionMemberConfig, ITableMetaData table)
	{
		IFieldMetaData versionField = table.getVersionField();
		if (versionField == null)
		{
			String versionColumnName;
			if (versionMemberConfig instanceof MemberConfig)
			{
				versionColumnName = ((MemberConfig) versionMemberConfig).getColumnName();
			}
			else
			{
				throw new IllegalStateException("'versionMemberConfig' not instance of '" + MemberConfig.class.getSimpleName() + "'");
			}
			if (versionColumnName != null && !versionColumnName.isEmpty() && table instanceof TableMetaData)
			{
				versionField = table.getFieldByName(versionColumnName);
				((TableMetaData) table).setVersionField(versionField);
			}
			else
			{
				throw new IllegalStateException("Cannot set version field");
			}
		}
	}

	protected void handleDescriminatorField(String descriminatorName, ITableMetaData table)
	{
		IFieldMetaData descriminatorField = table.getDescriminatorField();
		if (descriminatorField == null)
		{
			if (descriminatorName != null && !descriminatorName.isEmpty() && table instanceof TableMetaData)
			{
				descriminatorField = table.getFieldByName(descriminatorName);
				((TableMetaData) table).setDescriminatorField(descriminatorField);
			}
			else
			{
				throw new IllegalStateException("Cannot set descriminator field");
			}
		}
	}

	protected void addToUcPropertyMap(String memberName, IPropertyInfo propertyInfo, IMap<String, IPropertyInfo> ucPropertyMap, int maxNameLength)
	{
		ISet<String> producedNameCandidates = produceNameCandidates(memberName);
		for (String name : producedNameCandidates)
		{
			String assumedFieldName1 = ormPatternMatcher.buildFieldNameFromSoftName(name, maxNameLength);
			String assumedFieldName2 = assumedFieldName1 + "_ID";
			String assumedFieldName3 = assumedFieldName1 + "_id";

			if (!ucPropertyMap.putIfNotExists(assumedFieldName1, propertyInfo) && ucPropertyMap.get(assumedFieldName1) != propertyInfo)
			{
				// Property is not uniquely resolvable if transformed to uppercase
				// So it will be no candidate for automated field mapping!
				ucPropertyMap.put(assumedFieldName1, null);
			}
			if (!ucPropertyMap.putIfNotExists(assumedFieldName2, propertyInfo) && ucPropertyMap.get(assumedFieldName2) != propertyInfo)
			{
				// Property is not uniquely resolvable if transformed to uppercase
				// So it will be no candidate for automated field mapping!
				ucPropertyMap.put(assumedFieldName2, null);
			}
			if (!ucPropertyMap.putIfNotExists(assumedFieldName3, propertyInfo) && ucPropertyMap.get(assumedFieldName3) != propertyInfo)
			{
				// Property is not uniquely resolvable if transformed to uppercase
				// So it will be no candidate for automated field mapping!
				ucPropertyMap.put(assumedFieldName3, null);
			}
		}
	}

	protected ITableMetaData getTableByType(IDatabaseMetaData database, Class<?> entityType)
	{
		ITableMetaData table = null;

		List<ITableMetaData> tables = database.getTables();
		for (int i = tables.size(); i-- > 0;)
		{
			ITableMetaData loopTable = tables.get(i);
			if (loopTable.isArchive())
			{
				continue;
			}
			Class<?> tableEntity = loopTable.getEntityType();
			if (tableEntity != null && tableEntity.equals(entityType))
			{
				table = loopTable;
				break;
			}
		}
		if (table == null)
		{
			throw new IllegalArgumentException("No table could be autoresolved with entity name '" + entityType + "'");
		}

		return table;
	}

	protected void mapBasic(ITableMetaData table, MemberConfig memberConfig)
	{
		String memberName = memberConfig.getName();
		String fieldName = memberConfig.getColumnName();
		if (fieldName == null || fieldName.isEmpty())
		{
			fieldName = StringConversionHelper.camelCaseToUnderscore(objectCollector, memberName);
		}
		if (memberConfig.isIgnore())
		{
			table.mapIgnore(fieldName, memberName);
		}
		else
		{
			table.mapField(fieldName, memberName);
		}
	}

	protected String getFqJoinTableName(ITableMetaData table, RelationConfigLegathy relationConfig)
	{
		return getFqObjectName(table, relationConfig.getJoinTableName());
	}

	protected String getFqObjectName(ITableMetaData table, String objectName)
	{
		if (objectName == null || objectName.contains("."))
		{
			return connectionDialect.toDefaultCase(objectName);
		}
		Matcher matcher = fqToSoftTableNamePattern.matcher(table.getName());
		if (!matcher.matches())
		{
			throw new IllegalStateException("Must never happen");
		}
		return matcher.group(1) + "." + objectName;
	}

	protected void mapToOne(Connection connection, DatabaseMetaData database, ITableMetaData table, RelationConfigLegathy relationConfig)
	{
		String memberName = relationConfig.getName();
		Class<?> linkedEntityType = relationConfig.getLinkedEntityType();
		boolean doDelete = relationConfig.doDelete();
		boolean mayDelete = relationConfig.mayDelete();
		String joinTableName = getFqJoinTableName(table, relationConfig);
		String linkName;

		IEntityMetaData linkedEntityMetaData = entityMetaDataProvider.getMetaData(linkedEntityType, true);
		boolean linkedEntityLocal = linkedEntityMetaData == null || linkedEntityMetaData.isLocalEntity();
		if (linkedEntityLocal)
		{
			// Local entity
			ITableMetaData table2 = getTableByType(database, linkedEntityType);
			linkName = findLinkNameLocal(table, memberName, joinTableName, relationConfig, table2, database);
		}
		else
		{
			// External entity
			String toAttributeName = relationConfig.getToAttributeName();
			Member toMember = getMemberByTypeAndName(linkedEntityType, toAttributeName);

			boolean useLinkTable = !table.getName().equalsIgnoreCase(joinTableName);
			if (useLinkTable)
			{
				linkName = joinTableName;
				addToMemberToPreBuildLink(database, linkName, toMember);
			}
			else
			{
				String fromFieldName = relationConfig.getFromFieldName();
				String toFieldName = relationConfig.getToFieldName();
				linkName = createFkLinkToExternalEntity(connection, database, table, fromFieldName, toFieldName, linkedEntityType, toAttributeName, toMember,
						false);
			}
		}

		ILinkMetaData link = database.getLinkByName(linkName);
		if (link == null)
		{
			throw new IllegalArgumentException("Link with name '" + linkName + "' was not found");
		}
		if (log.isDebugEnabled())
		{
			log.debug("Mapping member '" + table.getEntityType().getName() + "." + memberName + "' to link '" + linkName + "'");
		}
		setCascadeValuesAndMapLink(table, link, memberName, doDelete, mayDelete);
	}

	protected void mapToMany(Connection connection, DatabaseMetaData database, ITableMetaData table, RelationConfigLegathy relationConfig)
	{
		String memberName = relationConfig.getName();
		Class<?> linkedEntityType = relationConfig.getLinkedEntityType();
		boolean doDelete = relationConfig.doDelete();
		boolean mayDelete = relationConfig.mayDelete();
		String joinTableName = getFqJoinTableName(table, relationConfig);
		String linkName;

		IEntityMetaData linkedEntityMetaData = entityMetaDataProvider.getMetaData(linkedEntityType, true);
		if (linkedEntityMetaData == null || linkedEntityMetaData.isLocalEntity())
		{
			// Local entity
			ITableMetaData table2 = getTableByType(database, linkedEntityType);

			linkName = findLinkNameLocal(table, memberName, joinTableName, relationConfig, table2, database);
		}
		else
		{
			// External entity
			String toAttributeName = relationConfig.getToAttributeName();
			Member toMember = getMemberByTypeAndName(linkedEntityType, toAttributeName);

			boolean useLinkTable = !table.getName().equalsIgnoreCase(joinTableName);
			if (useLinkTable)
			{
				linkName = joinTableName;
				addToMemberToPreBuildLink(database, linkName, toMember);
			}
			else
			{
				String fromFieldName = relationConfig.getFromFieldName();
				String toFieldName = relationConfig.getToFieldName();
				linkName = createFkLinkToExternalEntity(connection, database, table, fromFieldName, toFieldName, linkedEntityType, toAttributeName, toMember,
						true);
			}
		}
		ILinkMetaData link = database.getLinkByName(linkName);
		if (link == null)
		{
			throw new IllegalArgumentException("Link with name '" + linkName + "' was not found");
		}
		if (log.isDebugEnabled())
		{
			log.debug("Mapping member '" + table.getEntityType().getName() + "." + memberName + "' to link '" + linkName + "'");
		}
		setCascadeValuesAndMapLink(table, link, memberName, doDelete, mayDelete);
	}

	protected void mapRelation(RelationConfig20 relationConfig20, ITableMetaData table, DatabaseMetaData database, Connection connection)
	{
		String propertyName = relationConfig20.getName();
		ILinkConfig linkConfig = relationConfig20.getLink();
		String linkSource = linkConfig.getSource();
		String linkName = linkSource;

		Class<?> fromEntityType = table.getEntityType();
		IEntityMetaData fromEntityMetaData = entityMetaDataProvider.getMetaData(fromEntityType);
		Member property = fromEntityMetaData.getMemberByName(propertyName);
		Class<?> toEntityType = property.getElementType();

		IEntityMetaData toEntityMetaData = entityMetaDataProvider.getMetaData(toEntityType, true);
		boolean toEntityLocal = toEntityMetaData == null || toEntityMetaData.isLocalEntity();
		if (!toEntityLocal)
		{
			// External entity
			String toMemberName = ((ExternalLinkConfig) linkConfig).getTargetMember();
			Member toMember = toEntityMetaData.getMemberByName(toMemberName);

			String joinTableName = getFqObjectName(table, linkSource);
			boolean useLinkTable = !table.getName().equalsIgnoreCase(joinTableName);
			if (useLinkTable)
			{
				linkName = linkSource;
				addToMemberToPreBuildLink(database, linkName, toMember);
			}
			else
			{
				String fromFieldName = ((ExternalLinkConfig) linkConfig).getSourceColumn();
				Class<?> toRealType = property.getRealType();
				boolean toMany = !toRealType.equals(toEntityType);
				linkName = createFkLinkToExternalEntity(connection, database, table, fromFieldName, null, toRealType, toMemberName, toMember, toMany);
			}
		}

		ILinkMetaData link = findLinkForRelation(propertyName, linkName, table, database);

		EntityIdentifier entityIdentifier = relationConfig20.getEntityIdentifier();
		if (entityIdentifier == null)
		{
			entityIdentifier = link.getFromTable().equals(table) ? EntityIdentifier.LEFT : EntityIdentifier.RIGHT;
		}

		boolean[] cascadeDeletes = resolveCascadeDeletes(linkConfig, entityIdentifier);
		boolean doDelete = cascadeDeletes[0];
		boolean mayDelete = cascadeDeletes[1];

		if (log.isDebugEnabled())
		{
			log.debug("Mapping member '" + table.getEntityType().getName() + "." + propertyName + "' to link '" + link.getName() + "' defined by '"
					+ linkSource + "'");
		}

		setCascadeValuesAndMapLink(table, link, propertyName, entityIdentifier, doDelete, mayDelete);
	}

	protected void setCascadeValuesAndMapLink(ITableMetaData table, ILinkMetaData link, String propertyName, EntityIdentifier entityIdentifier,
			boolean doDelete, boolean mayDelete)
	{
		boolean localIsLeft = entityIdentifier == EntityIdentifier.LEFT;
		DirectedLinkMetaData localLinkMetaData = (DirectedLinkMetaData) (localIsLeft ? link.getDirectedLink() : link.getReverseDirectedLink());
		DirectedLinkMetaData remoteLinkMetaData = (DirectedLinkMetaData) (localIsLeft ? link.getReverseDirectedLink() : link.getDirectedLink());

		localLinkMetaData.setCascadeDelete(doDelete);
		remoteLinkMetaData.setCascadeDelete(mayDelete);
		if (localLinkMetaData.getMember() == null)
		{
			table.mapLink(localLinkMetaData.getName(), propertyName);
		}
	}

	protected boolean[] resolveCascadeDeletes(ILinkConfig linkConfig, EntityIdentifier entityIdentifier)
	{
		boolean[] cascadeDeletes;
		CascadeDeleteDirection cascadeDeleteDirection = linkConfig.getCascadeDeleteDirection();
		if (CascadeDeleteDirection.BOTH == cascadeDeleteDirection)
		{
			cascadeDeletes = new boolean[] { true, true };
		}
		else if (CascadeDeleteDirection.NONE == cascadeDeleteDirection)
		{
			cascadeDeletes = new boolean[] { false, false };
		}
		else
		{
			cascadeDeletes = new boolean[2];
			cascadeDeletes[0] = !entityIdentifier.name().equals(cascadeDeleteDirection.name());
			cascadeDeletes[1] = !cascadeDeletes[0];
		}
		return cascadeDeletes;
	}

	protected ILinkMetaData resolveLinkByTables(String propertyName, ITableMetaData table, IDatabaseMetaData database, ILinkMetaData link)
	{
		Member member = getMemberByTypeAndName(table.getEntityType(), propertyName);
		Class<?> relatedEntityType = member.getElementType();
		ITableMetaData relatedTable = getTableByType(database, relatedEntityType);
		ILinkMetaData[] links = database.getLinksByTables(table, relatedTable);
		if (links == null || links.length == 0)
		{
			throw new IllegalArgumentException("No Link found for '" + table.getEntityType().getName() + "." + propertyName + "'");
		}
		else if (links.length == 1)
		{
			link = links[0];
			return link;
		}
		else
		{
			throw new IllegalArgumentException("Unconfigured Link for '" + table.getEntityType().getName() + "." + propertyName
					+ "' not uniquely idenfifiable. Please provide the name in the orm.xml file.");
		}
	}

	private String findLinkNameLocal(ITableMetaData table, String memberName, String joinTableName, RelationConfigLegathy relationConfig,
			ITableMetaData table2, DatabaseMetaData database)
	{
		String linkName;
		if (joinTableName == null)
		{
			linkName = resolveLinkDynamically(database, memberName, relationConfig.getConstraintName(), table, table2);
		}
		else
		{
			boolean useLinkTable = !table.getName().equalsIgnoreCase(joinTableName) && !table2.getName().equalsIgnoreCase(joinTableName);
			if (!useLinkTable)
			{
				linkName = mapDataTableWithLink(database, table, table2, relationConfig, false);
				if (database.getLinkByName(linkName) == null)
				{
					String linkNameRetry = mapDataTableWithLink(database, table, table2, relationConfig, true);
					if (database.getLinkByName(linkNameRetry) != null)
					{
						linkName = linkNameRetry;
					}
				}
			}
			else
			{
				linkName = joinTableName;
			}
		}
		return linkName;
	}

	private String createFkLinkToExternalEntity(Connection connection, DatabaseMetaData database, ITableMetaData table, String fromFieldName,
			String toFieldName, Class<?> linkedEntityType, String toAttributeName, Member toMember, boolean toMany)
	{
		IFieldMetaData fromField = table.getFieldByName(fromFieldName);
		IFieldMetaData toField = null;
		if (toMany && toFieldName != null && !toFieldName.isEmpty())
		{
			toField = table.getFieldByName(toFieldName);
		}
		String linkName = database.createForeignKeyLinkName(table.getName(), fromFieldName, linkedEntityType.getSimpleName(), toAttributeName);
		buildAndMapLink(connection, database, linkName, table, fromField, toField, toMember);
		return linkName;
	}

	private ILinkMetaData findLinkForRelation(String propertyName, String linkName, ITableMetaData table, IDatabaseMetaData database)
	{
		ILinkMetaData link = null;
		if (!linkName.isEmpty())
		{
			link = database.getLinkByDefiningName(linkName);
			if (link == null)
			{
				link = database.getLinkByDefiningName(linkName.toUpperCase());
				if (link == null)
				{
					link = database.getLinkByName(linkName);
					if (link == null)
					{
						throw new IllegalArgumentException("Link defined by '" + linkName + "' was not found");
					}
				}
			}
		}
		else
		{
			link = resolveLinkByTables(propertyName, table, database, link);
		}
		return link;
	}

	protected String resolveLinkDynamically(IDatabaseMetaData database, String memberName, String constraintName, ITableMetaData table, ITableMetaData table2)
	{
		if (constraintName == null)
		{
			return null;
		}
		List<ILinkMetaData> links = database.getLinks();
		for (int a = links.size(); a-- > 0;)
		{
			ILinkMetaData link = links.get(a);
			if (!(link instanceof SqlLinkMetaData))
			{
				continue;
			}
			String constraintNameOfLink = ((SqlLinkMetaData) link).getConstraintName();
			if (constraintNameOfLink == null)
			{
				DirectedLinkMetaData linkForward = (DirectedLinkMetaData) link.getDirectedLink();
				DirectedLinkMetaData linkReverse = (DirectedLinkMetaData) link.getReverseDirectedLink();
				if (constraintName.equalsIgnoreCase(linkForward.getConstraintName()))
				{
					return linkForward.getName();
				}
				if (constraintName.equalsIgnoreCase(linkReverse.getConstraintName()))
				{
					return linkReverse.getName();
				}
			}
			if (constraintName.equalsIgnoreCase(constraintNameOfLink))
			{
				return link.getName();
			}
		}
		return null;
	}

	protected String mapDataTableWithLink(DatabaseMetaData database, ITableMetaData table, ITableMetaData table2, RelationConfigLegathy relationConfig,
			boolean reverse)
	{
		String joinTableName = getFqJoinTableName(table, relationConfig);
		ITableMetaData fromTable, toTable;
		if (table.getName().equalsIgnoreCase(joinTableName))
		{
			fromTable = table;
			toTable = table2;
		}
		else
		{
			fromTable = table2;
			toTable = table;
		}
		if (reverse)
		{
			ITableMetaData tempTable = fromTable;
			fromTable = toTable;
			toTable = tempTable;
		}
		String fromTableName = fromTable.getName();
		String fromFieldName = relationConfig.getFromFieldName();
		String toTableName = toTable.getName();
		String toFieldName = relationConfig.getToFieldName();
		if (reverse)
		{
			String tempFieldName = fromFieldName;
			fromFieldName = toFieldName;
			toFieldName = tempFieldName;
		}
		mapFieldToMember(table, joinTableName, fromFieldName, relationConfig.getName());
		String linkName = database.createForeignKeyLinkName(fromTableName, fromFieldName, toTableName, toFieldName);
		return linkName;
	}

	protected void mapFieldToMember(ITableMetaData table, String joinTableName, String fieldName, String memberName)
	{
		if (table.getName().equalsIgnoreCase(joinTableName) && table.getFieldByName(fieldName).getMember() == null)
		{
			table.mapField(fieldName, memberName);
			if (log.isDebugEnabled())
			{
				log.debug("Configuring member '" + table.getEntityType().getName() + "." + memberName + "' for database field '" + table.getName() + "."
						+ fieldName + "'");
			}
		}
	}

	protected void setCascadeValuesAndMapLink(ITableMetaData table, ILinkMetaData link, String memberName, boolean doDelete, boolean mayDelete)
	{
		if (table.equals(link.getFromTable()))
		{
			((DirectedLinkMetaData) link.getDirectedLink()).setCascadeDelete(doDelete);
			((DirectedLinkMetaData) link.getReverseDirectedLink()).setCascadeDelete(mayDelete);
			if (link.getDirectedLink().getMember() == null)
			{
				table.mapLink(link.getDirectedLink().getName(), memberName);
			}
		}
		else
		{
			((DirectedLinkMetaData) link.getDirectedLink()).setCascadeDelete(mayDelete);
			((DirectedLinkMetaData) link.getReverseDirectedLink()).setCascadeDelete(doDelete);
			if (link.getReverseDirectedLink().getMember() == null)
			{
				table.mapLink(link.getReverseDirectedLink().getName(), memberName);
			}
		}
	}

	protected void addToMemberToPreBuildLink(IDatabaseMetaData database, String linkName, Member toMember)
	{
		SqlLinkMetaData link = (SqlLinkMetaData) database.getLinkByName(linkName);

		DirectedExternalLinkMetaData directed = (DirectedExternalLinkMetaData) link.getDirectedLink();
		directed.setEntityMetaDataProvider(entityMetaDataProvider);
		directed.setToMember(toMember);
		directed.afterPropertiesSet();

		DirectedExternalLinkMetaData reverse = (DirectedExternalLinkMetaData) link.getReverseDirectedLink();
		reverse.setEntityMetaDataProvider(entityMetaDataProvider);
		reverse.setFromMember(toMember);
		reverse.afterPropertiesSet();

		((IConfigurableDatabaseMetaData) database).mapLink(link);
	}

	protected void handleExternalEntities()
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder debugSb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			for (IEntityConfig entityConfig : ormConfigGroup.getExternalEntityConfigs())
			{
				Class<?> entityType = entityConfig.getEntityType();
				Class<?> realType = entityConfig.getRealType();

				EntityMetaData metaData = new EntityMetaData();
				metaData.setEntityType(entityType);
				metaData.setRealType(realType);
				metaData.setLocalEntity(false);

				entityMetaDataReader.addMembers(metaData, entityConfig);

				if (metaData.getIdMember() == null)
				{
					throw new IllegalArgumentException("ID attribute missing in configuration for external entity '" + entityType.getName() + "'");
				}
				if (entityConfig.isVersionRequired() && metaData.getVersionMember() == null)
				{
					throw new IllegalArgumentException("Version attribute missing in configuration for external entity '" + entityType.getName() + "'");
				}

				synchronized (entityMetaDataExtendable)
				{
					if (entityMetaDataProvider.getMetaData(metaData.getEntityType(), true) == null)
					{
						entityMetaDataExtendable.registerEntityMetaData(metaData);
						registeredMetaDatas.add(metaData);
					}
				}
				if (log.isDebugEnabled())
				{
					debugSb.setLength(0);

					debugSb.append("Mapped external entity '").append(metaData.getEntityType().getName()).append("' with members ID ('")
							.append(metaData.getIdMember().getName());
					if (entityConfig.isVersionRequired())
					{
						debugSb.append("'), version ('").append(metaData.getVersionMember().getName()).append("')");
					}
					else
					{
						debugSb.append("'), no version");
					}
					debugPrintMembers(debugSb, metaData.getAlternateIdMembers(), "alternate IDs");
					debugPrintMembers(debugSb, metaData.getPrimitiveMembers(), "primitives");
					debugPrintMembers(debugSb, metaData.getRelationMembers(), "relations");

					log.debug(debugSb.toString());
				}
			}
		}
		finally
		{
			tlObjectCollector.dispose(debugSb);
		}
	}

	protected void debugPrintMembers(StringBuilder debugSb, Member[] toPrint, String membersName)
	{
		if (toPrint.length == 0)
		{
			debugSb.append(", no ").append(membersName);
		}
		else
		{
			debugSb.append(", ").append(membersName).append(" ('");
			for (int j = 0; j < toPrint.length; j++)
			{
				Member memberToPrint = toPrint[j];
				if (j > 0)
				{
					debugSb.append("', '");
				}
				debugSb.append(memberToPrint.getName());
			}
			debugSb.append("')");
		}
	}

	protected Member getMemberByTypeAndName(Class<?> entityType, String memberName)
	{
		Member member = memberTypeProvider.getMember(entityType, memberName);
		if (member == null)
		{
			throw new IllegalArgumentException("No entity member found for name '" + memberName + "' on entity '" + entityType.getName() + "'");
		}
		return member;
	}

	protected SqlLinkMetaData buildAndMapLink(Connection connection, IConfigurableDatabaseMetaData confDatabase, String linkName, ITableMetaData fromTable,
			IFieldMetaData fromField, IFieldMetaData toField, Member member)
	{
		SqlLinkMetaData link = new SqlLinkMetaData();
		boolean nullable;
		try
		{
			nullable = confDatabase.isFieldNullable(connection, fromField);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		if (!fromField.isAlternateId())
		{
			fromField.getTable().getPrimitiveFields().remove(fromField);
		}

		link.setName(linkName);
		link.setTableName(fromTable.getName());
		link.setFullqualifiedEscapedTableName(fromTable.getFullqualifiedEscapedName());
		link.setFromTable(fromTable);
		link.setFromField(fromField);
		link.setToField(toField);
		link.setNullable(nullable);

		DirectedExternalLinkMetaData directedLink = new DirectedExternalLinkMetaData();
		directedLink.setLink(link);
		directedLink.setFromTable(fromTable);
		directedLink.setFromField(fromField);
		directedLink.setToField(toField);
		directedLink.setToMember(member);
		directedLink.setStandaloneLink(fromField.isAlternateId() || fromTable.getIdField().equals(fromField));
		directedLink.setObjectCollector(objectCollector);
		directedLink.setEntityMetaDataProvider(entityMetaDataProvider);
		directedLink.afterPropertiesSet();

		DirectedExternalLinkMetaData revDirectedLink = new DirectedExternalLinkMetaData();
		revDirectedLink.setLink(link);
		revDirectedLink.setFromField(toField);
		revDirectedLink.setFromMember(member);
		revDirectedLink.setToTable(fromTable);
		revDirectedLink.setToField(fromField);
		revDirectedLink.setStandaloneLink(true);
		revDirectedLink.setObjectCollector(objectCollector);
		revDirectedLink.setEntityMetaDataProvider(entityMetaDataProvider);
		revDirectedLink.afterPropertiesSet();

		link.setDirectedLink(directedLink);
		link.setReverseDirectedLink(revDirectedLink);

		link = (SqlLinkMetaData) confDatabase.mapLink(link);

		return link;
	}

	@Override
	public void mapFields(IOrmConfigGroup ormConfigGroup)
	{
		List<ITableMetaData> tables = mapFieldsIntern(ormConfigGroup, databaseMetaData);
		IList<IDatabaseMappedListener> databaseMappedListeners = serviceContext.getObjects(IDatabaseMappedListener.class);

		for (ITableMetaData table : tables)
		{
			databaseMetaData.handleTable(table);
			for (IDatabaseMappedListener listener : databaseMappedListeners)
			{
				listener.newTableMetaData(table);
			}
		}

	}

}
