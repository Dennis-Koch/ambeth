package de.osthus.ambeth.orm;

import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.config.IEntityMetaDataReader;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.Database;
import de.osthus.ambeth.persistence.DirectedExternalLink;
import de.osthus.ambeth.persistence.DirectedLink;
import de.osthus.ambeth.persistence.IConfigurableDatabase;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ILink;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.Link;
import de.osthus.ambeth.persistence.Table;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.sql.SqlLink;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.ITypeInfo;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

public class XmlDatabaseMapper extends DefaultDatabaseMapper implements IStartingBean, IDisposableBean
{
	public static final Pattern fqToSoftTableNamePattern = Pattern.compile("\"?(.+?)\"?\\.\"?(.+?)\"?");

	public static final Pattern softTableNamePattern = Pattern.compile("\"?(.+?)\"?");

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

	protected Set<EntityConfig> localEntities = new LinkedHashSet<EntityConfig>();

	protected Set<EntityConfig> externalEntities = new LinkedHashSet<EntityConfig>();

	protected IServiceContext serviceContext;

	protected IThreadLocalObjectCollector objectCollector;

	protected IEntityMetaDataExtendable entityMetaDataExtendable;

	protected IEntityMetaDataReader entityMetaDataReader;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IOrmXmlReaderRegistry ormXmlReaderRegistry;

	protected IProperties properties;

	protected IPropertyInfoProvider propertyInfoProvider;

	protected IXmlConfigUtil xmlConfigUtil;

	protected String xmlFileName = null;

	protected Class<? extends SqlLink> linkType;

	protected final List<EntityMetaData> registeredMetaDatas = new ArrayList<EntityMetaData>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(entityMetaDataExtendable, "EntityMetaDataExtendable");
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
		ParamChecker.assertNotNull(entityMetaDataReader, "EntityMetaDataReader");
		ParamChecker.assertNotNull(serviceContext, "ServiceContext");
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
		ParamChecker.assertNotNull(properties, "Properties");
		ParamChecker.assertNotNull(propertyInfoProvider, "PropertyInfoProvider");
		ParamChecker.assertNotNull(xmlConfigUtil, "XmlConfigUtil");

		if (linkType == null)
		{
			linkType = SqlLink.class;
		}
	}

	@Override
	public void afterStarted() throws Throwable
	{
		if (xmlFileName != null)
		{
			Document[] docs = xmlConfigUtil.readXmlFiles(xmlFileName);
			ParamChecker.assertNotNull(docs, "docs");
			loadEntityMappings(docs);
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

	public void setEntityMetaDataExtendable(IEntityMetaDataExtendable entityMetaDataExtendable)
	{
		this.entityMetaDataExtendable = entityMetaDataExtendable;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setEntityMetaDataReader(IEntityMetaDataReader entityMetaDataReader)
	{
		this.entityMetaDataReader = entityMetaDataReader;
	}

	public void setServiceContext(IServiceContext serviceContext)
	{
		this.serviceContext = serviceContext;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setOrmXmlReaderRegistry(IOrmXmlReaderRegistry ormXmlReaderRegistry)
	{
		this.ormXmlReaderRegistry = ormXmlReaderRegistry;
	}

	public void setProperties(IProperties properties)
	{
		this.properties = properties;
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider)
	{
		this.propertyInfoProvider = propertyInfoProvider;
	}

	public void setXmlConfigUtil(IXmlConfigUtil xmlConfigUtil)
	{
		this.xmlConfigUtil = xmlConfigUtil;
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

	@Property(name = PersistenceConfigurationConstants.LinkClass, mandatory = false)
	public void setLinkType(Class<? extends SqlLink> linkType)
	{
		this.linkType = linkType;
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

	protected void loadEntityMappings(Document[] docs)
	{
		for (Document doc : docs)
		{
			doc.normalizeDocument();
			String documentNamespace = xmlConfigUtil.readDocumentNamespace(doc);
			IOrmXmlReader ormXmlReader = ormXmlReaderRegistry.getOrmXmlReader(documentNamespace);
			ormXmlReader.loadFromDocument(doc, localEntities, externalEntities);
		}
	}

	@Override
	public void mapFields(IDatabase database)
	{
		if (xmlFileName == null)
		{
			// No config source was set
			return;
		}

		if (!externalEntities.isEmpty())
		{
			handleExternalEntities();
		}
		Iterator<EntityConfig> iter = localEntities.iterator();
		while (iter.hasNext())
		{
			EntityConfig entityConfig = iter.next();

			Class<?> entityType = entityConfig.getEntityType();
			Class<?> realType = entityConfig.getRealType();
			String idName = this.idName;
			String versionName = this.versionName;

			ITable table = tryResolveTable(database, entityConfig.getTableName(), typeInfoProvider.getTypeInfo(realType).getSimpleName());
			table = database.mapTable(table.getName(), entityType);

			ITable archiveTable = database.getTableByName(archiveTablePrefix + table.getName() + archiveTablePostfix);
			if (archiveTable != null)
			{
				database.mapArchiveTable(archiveTable.getName(), entityType);
			}

			String sequenceName = entityConfig.getSequenceName();

			if (sequenceName == null)
			{
				Matcher matcher = fqToSoftTableNamePattern.matcher(table.getName());
				if (!matcher.matches())
				{
					throw new IllegalStateException("Must never happen");
				}
				sequenceName = sequencePrefix + matcher.group(2) + sequencePostfix;
			}
			sequenceName = getFqObjectName(table, sequenceName);
			if (table instanceof Table)
			{
				((Table) table).setSequenceName(sequenceName);
			}
			else
			{
				throw new IllegalStateException("Cannot set sequence name");
			}

			if (entityConfig.getIdMemberConfig() != null)
			{
				IMemberConfig idMemberConfig = entityConfig.getIdMemberConfig();
				idName = idMemberConfig.getName();
				if (idMemberConfig instanceof MemberConfig)
				{
					handleIdField((MemberConfig) idMemberConfig, table);
				}
				else
				{
					throw new IllegalStateException("Member configurations of type '" + idMemberConfig.getClass().getName() + "' not yet supported");
				}
			}

			if (entityConfig.isVersionRequired() && entityConfig.getVersionMemberConfig() != null)
			{
				IMemberConfig versionMemberConfig = entityConfig.getVersionMemberConfig();
				versionName = versionMemberConfig.getName();
				handleVersionField(versionMemberConfig, table);
			}

			HashSet<String> ignoredMembers = new HashSet<String>();
			Iterable<IMemberConfig> memberIter = entityConfig.getMemberConfigIterable();
			for (IMemberConfig memberConfig : memberIter)
			{
				if (!(memberConfig instanceof MemberConfig))
				{
					throw new IllegalStateException("Member configurations of type '" + memberConfig.getClass().getName() + "' not yet supported");
				}
				mapBasic(table, (MemberConfig) memberConfig);
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
				addToUcPropertyMap(memberName, propertyInfo, ucPropertyMap);
			}
			List<IField> fields = table.getAllFields();
			ArrayList<IField> fieldsCopy = new ArrayList<IField>(fields);
			Collections.sort(fieldsCopy, new Comparator<IField>()
			{
				@Override
				public int compare(IField o1, IField o2)
				{
					return o2.getName().compareTo(o1.getName()); // Reverse order
				}
			});

			for (int a = fieldsCopy.size(); a-- > 0;)
			{
				IField field = fieldsCopy.get(a);
				if (field.getMember() != null)
				{
					// Field already mapped
					continue;
				}
				String fieldName = field.getName().toUpperCase();
				IPropertyInfo propertyInfo = ucPropertyMap.get(fieldName);
				if (propertyInfo == null)
				{
					if (log.isDebugEnabled())
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
		}
		super.mapFields(database);
	}

	@Override
	public void mapLinks(IDatabase database)
	{
		if (xmlFileName == null)
		{
			// No config source was set
			return;
		}

		IConfigurableDatabase confDatabase = (IConfigurableDatabase) database;
		List<ILink> allLinks = database.getLinks();
		for (int i = allLinks.size(); i-- > 0;)
		{
			ILink link = allLinks.get(i);
			if (link.getName().equals(link.getTableName()))
			{
				String archiveTableName = archiveTablePrefix + link.getName() + archiveTablePostfix;
				if (confDatabase.isLinkArchiveTable(archiveTableName))
				{
					((Link) link).setArchiveTableName(archiveTableName);
				}
			}
		}

		Iterator<EntityConfig> iter = localEntities.iterator();
		while (iter.hasNext())
		{
			EntityConfig entityConfig = iter.next();

			Class<?> entityType = entityConfig.getEntityType();
			ITypeInfo typeInfo = typeInfoProvider.getTypeInfo(entityType);
			ITable table = getTableByType(database, entityType);

			Iterable<IRelationConfig> relationIter = entityConfig.getRelationConfigIterable();
			for (IRelationConfig relationConfig : relationIter)
			{
				if (relationConfig instanceof RelationConfigLegathy)
				{
					RelationConfigLegathy relationConfigLegathy = (RelationConfigLegathy) relationConfig;
					if (relationConfigLegathy.isToOne())
					{
						mapToOne((Database) database, table, relationConfigLegathy);
					}
					else
					{
						mapToMany((Database) database, table, relationConfigLegathy);
					}
				}
				else if (relationConfig instanceof RelationConfig20)
				{
					RelationConfig20 relationConfig20 = (RelationConfig20) relationConfig;
					mapRelation(typeInfo, relationConfig20, table, database);
				}
				else
				{
					throw new RuntimeException("RelationConfig of type '" + relationConfig.getClass().getSimpleName() + "' not yet supported");
				}
			}
		}

		super.mapLinks(database);
	}

	protected ITable tryResolveTable(IDatabase database, String hardName, String softName)
	{
		if (hardName != null && hardName.length() > 0)
		{
			ITable table = database.getTableByName(hardName.toUpperCase());
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
		String assumedName = tablePrefix + softName + tablePostfix;

		ITable table = null;
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

	protected void handleIdField(MemberConfig idMemberConfig, ITable table)
	{
		IField idField = table.getIdField();
		if (idField == null)
		{
			String idColumnName = idMemberConfig.getColumnName();
			if (idColumnName != null && !idColumnName.isEmpty() && table instanceof Table)
			{
				idField = table.getFieldByName(idColumnName);
				((Table) table).setIdField(idField);
			}
			else
			{
				throw new IllegalStateException("Cannot set id field");
			}
		}
	}

	protected void handleVersionField(IMemberConfig versionMemberConfig, ITable table)
	{
		IField versionField = table.getVersionField();
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
			if (versionColumnName != null && !versionColumnName.isEmpty() && table instanceof Table)
			{
				versionField = table.getFieldByName(versionColumnName);
				((Table) table).setVersionField(versionField);
			}
			else
			{
				throw new IllegalStateException("Cannot set version field");
			}
		}
	}

	protected void addToUcPropertyMap(String memberName, IPropertyInfo propertyInfo, IMap<String, IPropertyInfo> ucPropertyMap)
	{
		ISet<String> producedNameCandidates = produceNameCandidates(memberName);
		for (String name : producedNameCandidates)
		{
			String assumedFieldName1 = fieldPrefix + name + fieldPostfix;
			String assumedFieldName2 = assumedFieldName1 + "_ID";

			if (!ucPropertyMap.putIfNotExists(assumedFieldName1, propertyInfo))
			{
				// Property is not uniquely resolvable if transformed to uppercase
				// So it will be no candidate for automated field mapping!
				ucPropertyMap.put(assumedFieldName1, null);
			}
			if (!ucPropertyMap.putIfNotExists(assumedFieldName2, propertyInfo))
			{
				// Property is not uniquely resolvable if transformed to uppercase
				// So it will be no candidate for automated field mapping!
				ucPropertyMap.put(assumedFieldName2, null);
			}
		}
	}

	protected ITable getTableByType(IDatabase database, Class<?> entityType)
	{
		ITable table = null;

		List<ITable> tables = database.getTables();
		for (int i = tables.size(); i-- > 0;)
		{
			ITable loopTable = tables.get(i);
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

	protected void mapBasic(ITable table, MemberConfig memberConfig)
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

	protected String getFqJoinTableName(ITable table, RelationConfigLegathy relationConfig)
	{
		return getFqObjectName(table, relationConfig.getJoinTableName());
	}

	protected String getFqObjectName(ITable table, String objectName)
	{
		if (objectName == null || objectName.contains("."))
		{
			return objectName;
		}
		Matcher matcher = fqToSoftTableNamePattern.matcher(table.getName());
		if (!matcher.matches())
		{
			throw new IllegalStateException("Must never happen");
		}
		return "\"" + matcher.group(1) + "\".\"" + objectName + "\"";
	}

	protected void mapToOne(Database database, ITable table, RelationConfigLegathy relationConfig)
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
			ITable table2 = getTableByType(database, linkedEntityType);

			if (joinTableName == null)
			{
				linkName = resolveLinkDynamically(database, memberName, relationConfig.getConstraintName(), table, table2);
			}
			else
			{
				boolean useLinkTable = !table.getName().equals(joinTableName) && !table2.getName().equals(joinTableName);
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
		}
		else
		{
			// External entity
			String toAttributeName = relationConfig.getToAttributeName();
			ITypeInfoItem toMember = getMemberByTypeAndName(linkedEntityType, toAttributeName);

			boolean useLinkTable = !table.getName().equals(joinTableName);

			if (!useLinkTable)
			{
				String fromFieldName = relationConfig.getFromFieldName();
				linkName = database.createForeignKeyLinkName(table.getName(), fromFieldName, typeInfoProvider.getTypeInfo(linkedEntityType).getSimpleName(),
						toAttributeName);
				buildAndMapLink(database, linkName, table, table.getFieldByName(fromFieldName), null, toMember);
			}
			else
			{
				linkName = joinTableName;
				addToMemberToPreBuildLink(database, linkName, toMember);
			}
		}

		ILink link = database.getLinkByName(linkName);
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

	protected void mapToMany(Database database, ITable table, RelationConfigLegathy relationConfig)
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
			ITable table2 = getTableByType(database, linkedEntityType);

			if (joinTableName == null)
			{
				linkName = resolveLinkDynamically(database, memberName, relationConfig.getConstraintName(), table, table2);
			}
			else
			{
				boolean useLinkTable = !table.getName().equals(joinTableName) && !table2.getName().equals(joinTableName);

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
		}
		else
		{
			// External entity
			String toAttributeName = relationConfig.getToAttributeName();
			ITypeInfoItem toMember = getMemberByTypeAndName(linkedEntityType, toAttributeName);

			if (joinTableName.equals(table.getName()))
			{
				String fromFieldName = relationConfig.getFromFieldName();
				IField fromField = table.getFieldByName(fromFieldName);
				String toFieldName = relationConfig.getToFieldName();
				IField toField = null;
				if (toFieldName != null && !toFieldName.isEmpty())
				{
					toField = table.getFieldByName(toFieldName);
				}
				linkName = database.createForeignKeyLinkName(table.getName(), fromFieldName, typeInfoProvider.getTypeInfo(linkedEntityType).getSimpleName(),
						toAttributeName);
				buildAndMapLink(database, linkName, table, fromField, toField, toMember);
			}
			else
			{
				linkName = joinTableName;
				addToMemberToPreBuildLink(database, linkName, toMember);
			}
		}
		ILink link = database.getLinkByName(linkName);
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

	protected void mapRelation(ITypeInfo typeInfo, RelationConfig20 relationConfig20, ITable table, IDatabase database)
	{
		String propertyName = relationConfig20.getName();
		ILinkConfig linkConfig = relationConfig20.getLink();
		String linkSource = linkConfig.getSource();
		ILink link = null;
		if (!linkSource.isEmpty())
		{
			link = database.getLinkByDefiningName(linkSource);
			if (link == null)
			{
				throw new IllegalArgumentException("Link defined by '" + linkSource + "' was not found");
			}
		}
		else
		{
			link = resolveLinkByTables(propertyName, table, typeInfo, database, link);
		}

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
			log.debug("Mapping member '" + typeInfo.getRealType().getName() + "." + propertyName + "' to link '" + link.getName() + "' defined by '"
					+ linkSource + "'");
		}

		setCascadeValuesAndMapLink(table, link, propertyName, entityIdentifier, doDelete, mayDelete);
	}

	protected void setCascadeValuesAndMapLink(ITable table, ILink link, String propertyName, EntityIdentifier entityIdentifier, boolean doDelete,
			boolean mayDelete)
	{
		if (entityIdentifier == EntityIdentifier.LEFT)
		{
			((DirectedLink) link.getDirectedLink()).setCascadeDelete(doDelete);
			((DirectedLink) link.getReverseDirectedLink()).setCascadeDelete(mayDelete);
			if (link.getDirectedLink().getMember() == null)
			{
				table.mapLink(link.getDirectedLink().getName(), propertyName);
			}
		}
		else
		{
			((DirectedLink) link.getDirectedLink()).setCascadeDelete(mayDelete);
			((DirectedLink) link.getReverseDirectedLink()).setCascadeDelete(doDelete);
			if (link.getReverseDirectedLink().getMember() == null)
			{
				table.mapLink(link.getReverseDirectedLink().getName(), propertyName);
			}
		}
	}

	protected boolean[] resolveCascadeDeletes(ILinkConfig linkConfig, EntityIdentifier entityIdentifier)
	{
		boolean[] cascadeDeletes = new boolean[2];
		CascadeDeleteDirection cascadeDeleteDirection = linkConfig.getCascadeDeleteDirection();
		if (CascadeDeleteDirection.BOTH == cascadeDeleteDirection)
		{
			cascadeDeletes[0] = true;
			cascadeDeletes[1] = true;
		}
		else if (CascadeDeleteDirection.NONE == cascadeDeleteDirection)
		{
			cascadeDeletes[0] = false;
			cascadeDeletes[1] = false;
		}
		else
		{
			cascadeDeletes[0] = !entityIdentifier.toString().equals(cascadeDeleteDirection.toString());
			cascadeDeletes[1] = !cascadeDeletes[0];
		}
		return cascadeDeletes;
	}

	protected ILink resolveLinkByTables(String propertyName, ITable table, ITypeInfo typeInfo, IDatabase database, ILink link)
	{
		ITypeInfoItem member = typeInfo.getMemberByName(propertyName);
		Class<?> relatedEntityType = member.getElementType();
		ITable relatedTable = getTableByType(database, relatedEntityType);
		List<ILink> links = database.getLinksByTables(table, relatedTable);
		if (links == null || links.isEmpty())
		{
			throw new IllegalArgumentException("No Link found for '" + typeInfo.getRealType().getName() + "." + propertyName + "'");
		}
		else if (links.size() == 1)
		{
			link = links.get(0);
			return link;
		}
		else
		{
			throw new IllegalArgumentException("Unconfigured Link for '" + typeInfo.getRealType().getName() + "." + propertyName
					+ "' not uniquely idenfifiable. Please provide the name in the orm.xml file.");
		}
	}

	protected String resolveLinkDynamically(IDatabase database, String memberName, String constraintName, ITable table, ITable table2)
	{
		if (constraintName == null)
		{
			return null;
		}
		constraintName = constraintName.toUpperCase();
		List<ILink> links = database.getLinks();
		for (int a = links.size(); a-- > 0;)
		{
			ILink link = links.get(a);
			if (!(link instanceof SqlLink))
			{
				continue;
			}
			String constraintNameOfLink = ((SqlLink) link).getConstraintName();
			if (constraintNameOfLink == null)
			{
				DirectedLink linkForward = (DirectedLink) link.getDirectedLink();
				DirectedLink linkReverse = (DirectedLink) link.getReverseDirectedLink();
				if (constraintName.equals(linkForward.getConstraintName()))
				{
					return linkForward.getName();
				}
				if (constraintName.equals(linkReverse.getConstraintName()))
				{
					return linkReverse.getName();
				}
			}
			if (constraintName.equals(constraintNameOfLink))
			{
				return link.getName();
			}
		}
		return null;
	}

	protected String mapDataTableWithLink(Database database, ITable table, ITable table2, RelationConfigLegathy relationConfig, boolean reverse)
	{
		String joinTableName = getFqJoinTableName(table, relationConfig);
		ITable fromTable, toTable;
		if (table.getName().equals(joinTableName))
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
			ITable tempTable = fromTable;
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

	protected void mapFieldToMember(ITable table, String joinTableName, String fieldName, String memberName)
	{
		if (table.getName().equals(joinTableName) && table.getFieldByName(fieldName).getMember() == null)
		{
			table.mapField(fieldName, memberName);
			if (log.isDebugEnabled())
			{
				log.debug("Configuring member '" + table.getEntityType().getName() + "." + memberName + "' for database field '" + table.getName() + "."
						+ fieldName + "'");
			}
		}
	}

	protected void setCascadeValuesAndMapLink(ITable table, ILink link, String memberName, boolean doDelete, boolean mayDelete)
	{
		if (table.equals(link.getFromTable()))
		{
			((DirectedLink) link.getDirectedLink()).setCascadeDelete(doDelete);
			((DirectedLink) link.getReverseDirectedLink()).setCascadeDelete(mayDelete);
			if (link.getDirectedLink().getMember() == null)
			{
				table.mapLink(link.getDirectedLink().getName(), memberName);
			}
		}
		else
		{
			((DirectedLink) link.getDirectedLink()).setCascadeDelete(mayDelete);
			((DirectedLink) link.getReverseDirectedLink()).setCascadeDelete(doDelete);
			if (link.getReverseDirectedLink().getMember() == null)
			{
				table.mapLink(link.getReverseDirectedLink().getName(), memberName);
			}
		}
	}

	protected void addToMemberToPreBuildLink(IDatabase database, String linkName, ITypeInfoItem toMember)
	{
		SqlLink link = (SqlLink) database.getLinkByName(linkName);

		DirectedExternalLink directed = (DirectedExternalLink) link.getDirectedLink();
		directed.setEntityMetaDataProvider(entityMetaDataProvider);
		directed.setToMember(toMember);
		directed.afterPropertiesSet();

		DirectedExternalLink reverse = (DirectedExternalLink) link.getReverseDirectedLink();
		reverse.setEntityMetaDataProvider(entityMetaDataProvider);
		reverse.setFromMember(toMember);
		reverse.afterPropertiesSet();

		((IConfigurableDatabase) database).mapLink(link);
	}

	protected void handleExternalEntities()
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder debugSb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			Iterator<EntityConfig> iter = externalEntities.iterator();
			while (iter.hasNext())
			{
				EntityConfig entityConfig = iter.next();

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

	protected void debugPrintMembers(StringBuilder debugSb, ITypeInfoItem[] toPrint, String membersName)
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
				ITypeInfoItem memberToPrint = toPrint[j];
				if (j > 0)
				{
					debugSb.append("', '");
				}
				debugSb.append(memberToPrint.getName());
			}
			debugSb.append("')");
		}
	}

	protected ITypeInfoItem getMemberByTypeAndName(Class<?> entityType, String memberName)
	{
		ITypeInfo typeInfo = typeInfoProvider.getTypeInfo(entityType);
		return getMemberByTypeAndName(typeInfo, memberName);
	}

	protected ITypeInfoItem getMemberByTypeAndName(ITypeInfo typeInfo, String memberName)
	{
		ITypeInfoItem member = typeInfo.getMemberByName(memberName);
		if (member == null)
		{
			throw new IllegalArgumentException("No entity member found for name '" + memberName + "' on entity '" + typeInfo.getRealType().getName() + "'");
		}
		return member;
	}

	protected SqlLink buildAndMapLink(IConfigurableDatabase confDatabase, String linkName, ITable fromTable, IField fromField, IField toField,
			ITypeInfoItem member)
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
		boolean nullable;
		try
		{
			nullable = confDatabase.isFieldNullable(fromField);
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
		link.setFromTable(fromTable);
		link.setFromField(fromField);
		link.setToField(toField);
		link.setNullable(nullable);

		DirectedExternalLink directedLink = new DirectedExternalLink();
		directedLink.setLink(link);
		directedLink.setFromTable(fromTable);
		directedLink.setFromField(fromField);
		directedLink.setToField(toField);
		directedLink.setToMember(member);
		directedLink.setStandaloneLink(fromField.isAlternateId() || fromTable.getIdField().equals(fromField));
		directedLink.setObjectCollector(objectCollector);
		directedLink.setEntityMetaDataProvider(entityMetaDataProvider);
		directedLink.afterPropertiesSet();

		DirectedExternalLink revDirectedLink = new DirectedExternalLink();
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

		link = (SqlLink) confDatabase.mapLink(link);

		return link;
	}

}
