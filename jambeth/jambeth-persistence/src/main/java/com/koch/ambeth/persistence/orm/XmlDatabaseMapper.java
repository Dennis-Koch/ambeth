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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.ExtendableContainer;
import com.koch.ambeth.ioc.extendable.IExtendableContainer;
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
import com.koch.ambeth.merge.orm.ILinkConfig;
import com.koch.ambeth.merge.orm.IMemberConfig;
import com.koch.ambeth.merge.orm.IOrmConfigGroup;
import com.koch.ambeth.merge.orm.IOrmConfigGroupExtendable;
import com.koch.ambeth.merge.orm.IOrmConfigGroupProvider;
import com.koch.ambeth.merge.orm.MemberConfig;
import com.koch.ambeth.merge.orm.RelationConfig20;
import com.koch.ambeth.merge.orm.RelationConfigLegathy;
import com.koch.ambeth.merge.orm.blueprint.IOrmDatabaseMapper;
import com.koch.ambeth.persistence.DatabaseMetaData;
import com.koch.ambeth.persistence.DirectedExternalLinkMetaData;
import com.koch.ambeth.persistence.DirectedLinkMetaData;
import com.koch.ambeth.persistence.FieldMetaData;
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
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.StringConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.ISetEntry;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class XmlDatabaseMapper extends DefaultDatabaseMapper implements IDisposableBean, IOrmDatabaseMapper, IOrmConfigGroupExtendable {

    public static final Pattern fqToSoftTableNamePattern = Pattern.compile("[\"`]?(.+?)[\"`]?\\.[\"`]?(.+?)[\"`]?");

    public static final Pattern softTableNamePattern = Pattern.compile("[\"`]?(.+?)[\"`]?");

    public static String[] splitSchemaAndName(String fqName) {
        var splitMatcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(fqName);
        String schemaName = null, softName = null;
        if (splitMatcher.matches()) {
            schemaName = splitMatcher.group(1);
            softName = splitMatcher.group(2);
        } else {
            splitMatcher = XmlDatabaseMapper.softTableNamePattern.matcher(fqName);
            if (splitMatcher.matches()) {
                // set "default" schema name
                softName = splitMatcher.group(1);
            } else {
                throw new IllegalArgumentException("Illegal full qualified name '" + fqName + "'");
            }
        }
        return new String[] { schemaName, softName };
    }

    protected final List<EntityMetaData> registeredMetaDatas = new ArrayList<>();
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
    protected IExtendableContainer<IOrmConfigGroup> ormConfigGroups = new ExtendableContainer<>(IOrmConfigGroup.class, "ormConfigGroup");
    protected HashSet<String> allFullqualifiedSequences;
    @Autowired
    protected IDatabaseMetaData databaseMetaData;
    @LogInstance
    private ILogger log;

    @Override
    public void destroy() throws Throwable {
        for (int a = registeredMetaDatas.size(); a-- > 0; ) {
            entityMetaDataExtendable.unregisterEntityMetaData(registeredMetaDatas.get(a));
        }
    }

    @Override
    public void mapFields(Connection connection, String[] schemaNames, IDatabaseMetaData database) {
        allFullqualifiedSequences = new HashSet<String>(connectionDialect.getAllFullqualifiedSequences(connection, schemaNames)) {
            @Override
            protected boolean equalKeys(String key, ISetEntry<String> entry) {
                return key.equalsIgnoreCase(entry.getKey());
            }

            @Override
            protected int extractHash(String key) {
                return key.toLowerCase().hashCode();
            }
        };
        handleExternalEntities();
        mapFieldsIntern(database, ormConfigGroups.getExtensionsShared());
        super.mapFields(connection, schemaNames, database);
    }

    protected List<ITableMetaData> mapFieldsIntern(IDatabaseMetaData database, IOrmConfigGroup... ormConfigGroups) {
        var tables = new ArrayList<ITableMetaData>();
        var maxNameLength = database.getMaxNameLength();
        for (var ormConfigGroup : ormConfigGroups) {
            for (var entityConfig : ormConfigGroup.getLocalEntityConfigs()) {
                var entityType = entityConfig.getEntityType();
                var realType = entityConfig.getRealType();
                var idName = this.idName;
                var versionName = this.versionName;

                var table = tryResolveTable(database, entityConfig.getTableName(), realType.getSimpleName());
                table = database.mapTable(table.getName(), entityType);

                var archiveTable = database.getTableByName(ormPatternMatcher.buildArchiveFromTableName(table.getName(), maxNameLength));
                if (archiveTable != null) {
                    database.mapArchiveTable(archiveTable.getName(), entityType);
                }

                configureTableSequence(table, entityConfig.getSequenceName(), maxNameLength, allFullqualifiedSequences);

                var pgTable = database.getTableByName(ormPatternMatcher.buildPermissionGroupFromTableName(table.getName(), maxNameLength));
                if (pgTable != null) {
                    configureTableSequence(pgTable, null, maxNameLength, allFullqualifiedSequences);
                }

                if (entityConfig.getIdMemberConfig() != null) {
                    var idMemberConfig = entityConfig.getIdMemberConfig();
                    idName = idMemberConfig.getName();
                    if (idMemberConfig instanceof MemberConfig) {
                        handleIdField(new MemberConfig[] { (MemberConfig) idMemberConfig }, table);
                    } else {
                        handleIdField(((CompositeMemberConfig) idMemberConfig).getMembers(), table);
                    }
                }

                if (entityConfig.isVersionRequired() && entityConfig.getVersionMemberConfig() != null) {
                    var versionMemberConfig = entityConfig.getVersionMemberConfig();
                    versionName = versionMemberConfig.getName();
                    handleVersionField(versionMemberConfig, table);
                }
                if (entityConfig.getDescriminatorName() != null) {
                    handleDescriminatorField(entityConfig.getDescriminatorName(), table);
                }

                var ignoredMembers = new HashSet<String>();
                var memberIter = entityConfig.getMemberConfigIterable();
                for (var memberConfig : memberIter) {
                    if (memberConfig.isTransient()) {
                        continue;
                    }
                    if (memberConfig instanceof CompositeMemberConfig) {
                        var members = ((CompositeMemberConfig) memberConfig).getMembers();
                        for (var member : members) {
                            mapBasic(table, member);
                        }
                    } else {
                        mapBasic(table, (MemberConfig) memberConfig);
                    }
                    if (memberConfig.isIgnore()) {
                        ignoredMembers.add(memberConfig.getName());
                        continue;
                    }
                }

                if (entityConfig.isVersionRequired() && table.getVersionField() == null) {
                    throw new IllegalStateException("No version field found in table '" + table.getName() + "'");
                }

                var propertyMap = propertyInfoProvider.getPropertyMap(entityType);
                var ucPropertyMap = new HashMap<String, IPropertyInfo>();
                for (var entry : propertyMap) {
                    var memberName = entry.getKey();
                    if (ignoredMembers.contains(memberName) || table.getFieldByMemberName(memberName) != null) {
                        continue;
                    }
                    var propertyInfo = entry.getValue();
                    var modifiers = propertyInfo.getModifiers();
                    if (Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers)) {
                        continue;
                    }
                    addToUcPropertyMap(memberName, propertyInfo, ucPropertyMap, maxNameLength);
                }
                var fields = table.getAllFields();
                var fieldsCopy = new ArrayList<>(fields);
                Collections.sort(fieldsCopy, new Comparator<IFieldMetaData>() {
                    @Override
                    public int compare(IFieldMetaData o1, IFieldMetaData o2) {
                        return o2.getName().compareTo(o1.getName()); // Reverse order
                    }
                });

                for (int a = fieldsCopy.size(); a-- > 0; ) {
                    IFieldMetaData field = fieldsCopy.get(a);
                    if (field.getMember() != null) {
                        // Field already mapped
                        continue;
                    }
                    var fieldName = field.getName();
                    var propertyInfo = ucPropertyMap.get(fieldName);
                    if (propertyInfo == null) {
                        if (field.expectsMapping() && log.isDebugEnabled()) {
                            log.debug("No member specified or autoresolved on entity '" + entityType.getName() + "' for database field '" + table.getName() + "." + field.getName() + "'");
                        }
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Autoresolving member '" + entityType.getName() + "." + propertyInfo.getName() + "' for database field '" + table.getName() + "." + field.getName() + "'");
                    }
                    table.mapField(field.getName(), propertyInfo.getName());
                }
                mapIdAndVersion(table, idName, versionName);
                tables.add(table);
            }
        }
        return tables;
    }

    protected void configureTableSequence(ITableMetaData table, String sequenceName, int maxNameLength, ISet<String> allFullqualifiedSequences) {
        if (sequenceName == null) {
            sequenceName = ormPatternMatcher.buildSequenceFromTableName(table.getName(), maxNameLength);
        }
        var fqSequenceName = getFqObjectName(table, sequenceName);
        fqSequenceName = allFullqualifiedSequences.get(fqSequenceName); // important: set the camelCase
        // of the existing sequence in
        // the database
        if (fqSequenceName == null) {
            fqSequenceName = sequenceName;
        }
        ((TableMetaData) table).setSequenceName(fqSequenceName);
    }

    @Override
    public void mapLinks(Connection connection, String[] schemaNames, IDatabaseMetaData database) {
        var maxNameLength = database.getMaxNameLength();
        var confDatabase = (IConfigurableDatabaseMetaData) database;
        var allLinks = database.getLinks();
        for (int i = allLinks.size(); i-- > 0; ) {
            var link = allLinks.get(i);
            if (link.getName().equalsIgnoreCase(link.getTableName())) {
                var archiveTableName = ormPatternMatcher.buildArchiveFromTableName(link.getName(), maxNameLength);
                if (confDatabase.isLinkArchiveTable(archiveTableName)) {
                    ((LinkMetaData) link).setArchiveTableName(archiveTableName);
                }
            }
        }

        DatabaseMetaData databaseImpl = (DatabaseMetaData) database;
        for (var ormConfigGroup : ormConfigGroups.getExtensionsShared()) {
            for (var entityConfig : ormConfigGroup.getLocalEntityConfigs()) {
                var entityType = entityConfig.getEntityType();

                var table = getTableByType(database, entityType);

                var relationIter = entityConfig.getRelationConfigIterable();
                for (var relationConfig : relationIter) {
                    if (relationConfig instanceof RelationConfigLegathy) {
                        var relationConfigLegathy = (RelationConfigLegathy) relationConfig;
                        if (relationConfigLegathy.isToOne()) {
                            mapToOne(connection, databaseImpl, table, relationConfigLegathy);
                        } else {
                            mapToMany(connection, databaseImpl, table, relationConfigLegathy);
                        }
                    } else if (relationConfig instanceof RelationConfig20) {
                        var relationConfig20 = (RelationConfig20) relationConfig;
                        mapRelation(relationConfig20, table, databaseImpl, connection);
                    } else {
                        throw new RuntimeException("RelationConfig of type '" + relationConfig.getClass().getSimpleName() + "' not yet supported");
                    }
                }
            }
        }
        super.mapLinks(connection, schemaNames, database);
    }

    protected ITableMetaData tryResolveTable(IDatabaseMetaData database, String hardName, String softName) {
        if (hardName != null && hardName.length() > 0) {
            var table = database.getTableByName(connectionDialect.toDefaultCase(hardName));
            if (table == null) {
                throw new IllegalArgumentException("No table found with name '" + hardName + "'");
            }
            return table;
        }
        if (softName == null || softName.length() == 0) {
            throw new IllegalArgumentException("No table name specified");
        }
        int maxProcedureNameLength = database.getMaxNameLength();
        var assumedName = ormPatternMatcher.buildTableNameFromSoftName(softName, maxProcedureNameLength);

        ITableMetaData table = null;
        {
            var producedNameCandidates = produceNameCandidates(assumedName);
            for (var name : producedNameCandidates) {
                table = database.getTableByName(name);
                if (table != null) {
                    break;
                }
            }
            if (table == null) {
                var tables = database.getTables();
                for (var name : producedNameCandidates) {
                    for (var currTable : tables) {
                        var schemaAndTableName = sqlBuilder.getSchemaAndTableName(currTable.getName());
                        if (schemaAndTableName[1].equalsIgnoreCase(name)) {
                            table = currTable;
                            break;
                        }
                    }
                }
            }
        }
        if (table == null) {
            throw new IllegalArgumentException("No table could be autoresolved with entity name '" + softName + "' in schema '" + database.getSchemaNames()[0] + "'");
        }
        return table;
    }

    protected ISet<String> produceNameCandidates(String assumedName) {
        var set = new HashSet<String>();
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

    protected void handleIdField(MemberConfig[] idMemberConfig, ITableMetaData table) {
        var idFields = table.getIdFields();
        if (idFields != null) {
            return;
        }
        idFields = new IFieldMetaData[idMemberConfig.length];
        for (int a = 0, size = idMemberConfig.length; a < size; a++) {
            var idColumnName = idMemberConfig[a].getColumnName();
            if (idColumnName != null && !idColumnName.isEmpty() && table instanceof TableMetaData) {
                var idField = table.getFieldByName(idColumnName);
                ((FieldMetaData) idField).setIdIndex(IObjRef.PRIMARY_KEY_INDEX);
                idFields[a] = idField;
            } else {
                throw new IllegalStateException("Cannot set id field");
            }
        }
        ((TableMetaData) table).setIdFields(idFields);
    }

    protected void handleVersionField(IMemberConfig versionMemberConfig, ITableMetaData table) {
        var versionField = table.getVersionField();
        if (versionField == null) {
            String versionColumnName;
            if (versionMemberConfig instanceof MemberConfig) {
                versionColumnName = ((MemberConfig) versionMemberConfig).getColumnName();
            } else {
                throw new IllegalStateException("'versionMemberConfig' not instance of '" + MemberConfig.class.getSimpleName() + "'");
            }
            if (versionColumnName != null && !versionColumnName.isEmpty() && table instanceof TableMetaData) {
                versionField = table.getFieldByName(versionColumnName);
                ((TableMetaData) table).setVersionField(versionField);
            } else {
                throw new IllegalStateException("Cannot set version field");
            }
        }
    }

    protected void handleDescriminatorField(String descriminatorName, ITableMetaData table) {
        var descriminatorField = table.getDescriminatorField();
        if (descriminatorField == null) {
            if (descriminatorName != null && !descriminatorName.isEmpty() && table instanceof TableMetaData) {
                descriminatorField = table.getFieldByName(descriminatorName);
                ((TableMetaData) table).setDescriminatorField(descriminatorField);
            } else {
                throw new IllegalStateException("Cannot set descriminator field");
            }
        }
    }

    protected void addToUcPropertyMap(String memberName, IPropertyInfo propertyInfo, IMap<String, IPropertyInfo> ucPropertyMap, int maxNameLength) {
        var producedNameCandidates = produceNameCandidates(memberName);
        for (var name : producedNameCandidates) {
            var assumedFieldName1 = ormPatternMatcher.buildFieldNameFromSoftName(name, maxNameLength);
            var assumedFieldName2 = assumedFieldName1 + "_ID";
            var assumedFieldName3 = assumedFieldName1 + "_id";

            if (!ucPropertyMap.putIfNotExists(assumedFieldName1, propertyInfo) && ucPropertyMap.get(assumedFieldName1) != propertyInfo) {
                // Property is not uniquely resolvable if transformed to uppercase
                // So it will be no candidate for automated field mapping!
                ucPropertyMap.put(assumedFieldName1, null);
            }
            if (!ucPropertyMap.putIfNotExists(assumedFieldName2, propertyInfo) && ucPropertyMap.get(assumedFieldName2) != propertyInfo) {
                // Property is not uniquely resolvable if transformed to uppercase
                // So it will be no candidate for automated field mapping!
                ucPropertyMap.put(assumedFieldName2, null);
            }
            if (!ucPropertyMap.putIfNotExists(assumedFieldName3, propertyInfo) && ucPropertyMap.get(assumedFieldName3) != propertyInfo) {
                // Property is not uniquely resolvable if transformed to uppercase
                // So it will be no candidate for automated field mapping!
                ucPropertyMap.put(assumedFieldName3, null);
            }
        }
    }

    protected ITableMetaData getTableByType(IDatabaseMetaData database, Class<?> entityType) {
        ITableMetaData table = null;

        var tables = database.getTables();
        for (int i = tables.size(); i-- > 0; ) {
            var loopTable = tables.get(i);
            if (loopTable.isArchive()) {
                continue;
            }
            Class<?> tableEntity = loopTable.getEntityType();
            if (tableEntity != null && tableEntity.equals(entityType)) {
                table = loopTable;
                break;
            }
        }
        if (table == null) {
            throw new IllegalArgumentException("No table could be autoresolved with entity name '" + entityType + "'");
        }

        return table;
    }

    protected void mapBasic(ITableMetaData table, MemberConfig memberConfig) {
        var memberName = memberConfig.getName();
        var fieldName = memberConfig.getColumnName();
        if (fieldName == null || fieldName.isEmpty()) {
            fieldName = StringConversionHelper.camelCaseToUnderscore(objectCollector, memberName);
        }
        if (memberConfig.isIgnore()) {
            table.mapIgnore(fieldName, memberName);
        } else {
            table.mapField(fieldName, memberName);
        }
    }

    protected String getFqJoinTableName(ITableMetaData table, RelationConfigLegathy relationConfig) {
        return getFqObjectName(table, relationConfig.getJoinTableName());
    }

    protected String getFqObjectName(ITableMetaData table, String objectName) {
        if (objectName == null || objectName.contains(".")) {
            return connectionDialect.toDefaultCase(objectName);
        }
        var matcher = fqToSoftTableNamePattern.matcher(table.getName());
        if (!matcher.matches()) {
            throw new IllegalStateException("Must never happen");
        }
        return matcher.group(1) + "." + objectName;
    }

    protected void mapToOne(Connection connection, DatabaseMetaData database, ITableMetaData table, RelationConfigLegathy relationConfig) {
        var memberName = relationConfig.getName();
        var linkedEntityType = relationConfig.getLinkedEntityType();
        var doDelete = relationConfig.doDelete();
        var mayDelete = relationConfig.mayDelete();
        var joinTableName = getFqJoinTableName(table, relationConfig);
        String linkName;

        var linkedEntityMetaData = entityMetaDataProvider.getMetaData(linkedEntityType, true);
        var linkedEntityLocal = linkedEntityMetaData == null || linkedEntityMetaData.isLocalEntity();
        if (linkedEntityLocal) {
            // Local entity
            var table2 = getTableByType(database, linkedEntityType);
            linkName = findLinkNameLocal(table, memberName, joinTableName, relationConfig, table2, database);
        } else {
            // External entity
            var toAttributeName = relationConfig.getToAttributeName();
            var toMember = getMemberByTypeAndName(linkedEntityType, toAttributeName);

            var useLinkTable = !table.getName().equalsIgnoreCase(joinTableName);
            if (useLinkTable) {
                linkName = joinTableName;
                addToMemberToPreBuildLink(database, linkName, toMember);
            } else {
                var fromFieldName = relationConfig.getFromFieldName();
                var toFieldName = relationConfig.getToFieldName();
                linkName = createFkLinkToExternalEntity(connection, database, table, fromFieldName, toFieldName, linkedEntityType, toAttributeName, toMember, false);
            }
        }

        var link = database.getLinkByName(linkName);
        if (link == null) {
            throw new IllegalArgumentException("Link with name '" + linkName + "' was not found");
        }
        if (log.isDebugEnabled()) {
            log.debug("Mapping member '" + table.getEntityType().getName() + "." + memberName + "' to link '" + linkName + "'");
        }
        setCascadeValuesAndMapLink(table, link, memberName, doDelete, mayDelete);
    }

    protected void mapToMany(Connection connection, DatabaseMetaData database, ITableMetaData table, RelationConfigLegathy relationConfig) {
        var memberName = relationConfig.getName();
        var linkedEntityType = relationConfig.getLinkedEntityType();
        var doDelete = relationConfig.doDelete();
        var mayDelete = relationConfig.mayDelete();
        var joinTableName = getFqJoinTableName(table, relationConfig);
        String linkName;

        var linkedEntityMetaData = entityMetaDataProvider.getMetaData(linkedEntityType, true);
        if (linkedEntityMetaData == null || linkedEntityMetaData.isLocalEntity()) {
            // Local entity
            var table2 = getTableByType(database, linkedEntityType);

            linkName = findLinkNameLocal(table, memberName, joinTableName, relationConfig, table2, database);
        } else {
            // External entity
            var toAttributeName = relationConfig.getToAttributeName();
            var toMember = getMemberByTypeAndName(linkedEntityType, toAttributeName);

            var useLinkTable = !table.getName().equalsIgnoreCase(joinTableName);
            if (useLinkTable) {
                linkName = joinTableName;
                addToMemberToPreBuildLink(database, linkName, toMember);
            } else {
                var fromFieldName = relationConfig.getFromFieldName();
                var toFieldName = relationConfig.getToFieldName();
                linkName = createFkLinkToExternalEntity(connection, database, table, fromFieldName, toFieldName, linkedEntityType, toAttributeName, toMember, true);
            }
        }
        var link = database.getLinkByName(linkName);
        if (link == null) {
            throw new IllegalArgumentException("Link with name '" + linkName + "' was not found");
        }
        if (log.isDebugEnabled()) {
            log.debug("Mapping member '" + table.getEntityType().getName() + "." + memberName + "' to link '" + linkName + "'");
        }
        setCascadeValuesAndMapLink(table, link, memberName, doDelete, mayDelete);
    }

    protected void mapRelation(RelationConfig20 relationConfig20, ITableMetaData table, DatabaseMetaData database, Connection connection) {
        var propertyName = relationConfig20.getName();
        var linkConfig = relationConfig20.getLink();
        var linkSource = linkConfig.getSource();
        var linkName = linkSource;

        var fromEntityType = table.getEntityType();
        var fromEntityMetaData = entityMetaDataProvider.getMetaData(fromEntityType);
        var property = fromEntityMetaData.getMemberByName(propertyName);
        var toEntityType = property.getElementType();

        var toEntityMetaData = entityMetaDataProvider.getMetaData(toEntityType, true);
        var toEntityLocal = toEntityMetaData == null || toEntityMetaData.isLocalEntity();
        if (!toEntityLocal) {
            // External entity
            var toMemberName = ((ExternalLinkConfig) linkConfig).getTargetMember();
            var toMember = toEntityMetaData.getMemberByName(toMemberName);

            var joinTableName = getFqObjectName(table, linkSource);
            var useLinkTable = !table.getName().equalsIgnoreCase(joinTableName);
            if (useLinkTable) {
                linkName = linkSource;
                addToMemberToPreBuildLink(database, linkName, toMember);
            } else {
                var fromFieldName = ((ExternalLinkConfig) linkConfig).getSourceColumn();
                var toRealType = property.getRealType();
                var toMany = !toRealType.equals(toEntityType);
                linkName = createFkLinkToExternalEntity(connection, database, table, fromFieldName, null, toRealType, toMemberName, toMember, toMany);
            }
        }

        var link = findLinkForRelation(propertyName, linkName, table, database);
        if (link == null) {
            if (log.isDebugEnabled()) {
                log.debug("Could not resolve link '" + linkName + " for member '" + table.getEntityType().getName() + "." + propertyName + " defined by '" + linkSource + "'");
            }
            return;
        }

        var entityIdentifier = relationConfig20.getEntityIdentifier();
        if (entityIdentifier == null) {
            entityIdentifier = link.getFromTable().equals(table) ? EntityIdentifier.LEFT : EntityIdentifier.RIGHT;
        }

        var cascadeDeletes = resolveCascadeDeletes(linkConfig, entityIdentifier);
        var doDelete = cascadeDeletes[0];
        var mayDelete = cascadeDeletes[1];

        if (log.isDebugEnabled()) {
            log.debug("Mapping member '" + table.getEntityType().getName() + "." + propertyName + "' to link '" + link.getName() + "' defined by '" + linkSource + "'");
        }

        setCascadeValuesAndMapLink(table, link, propertyName, entityIdentifier, doDelete, mayDelete);
    }

    protected void setCascadeValuesAndMapLink(ITableMetaData table, ILinkMetaData link, String propertyName, EntityIdentifier entityIdentifier, boolean doDelete, boolean mayDelete) {
        var localIsLeft = entityIdentifier == EntityIdentifier.LEFT;
        var localLinkMetaData = (DirectedLinkMetaData) (localIsLeft ? link.getDirectedLink() : link.getReverseDirectedLink());
        var remoteLinkMetaData = (DirectedLinkMetaData) (localIsLeft ? link.getReverseDirectedLink() : link.getDirectedLink());

        localLinkMetaData.setCascadeDelete(doDelete);
        remoteLinkMetaData.setCascadeDelete(mayDelete);
        if (localLinkMetaData.getMember() == null) {
            table.mapLink(localLinkMetaData.getName(), propertyName);
        }
    }

    protected boolean[] resolveCascadeDeletes(ILinkConfig linkConfig, EntityIdentifier entityIdentifier) {
        boolean[] cascadeDeletes;
        var cascadeDeleteDirection = linkConfig.getCascadeDeleteDirection();
        if (CascadeDeleteDirection.BOTH == cascadeDeleteDirection) {
            cascadeDeletes = new boolean[] { true, true };
        } else if (CascadeDeleteDirection.NONE == cascadeDeleteDirection) {
            cascadeDeletes = new boolean[] { false, false };
        } else {
            cascadeDeletes = new boolean[2];
            cascadeDeletes[0] = !entityIdentifier.name().equals(cascadeDeleteDirection.name());
            cascadeDeletes[1] = !cascadeDeletes[0];
        }
        return cascadeDeletes;
    }

    protected ILinkMetaData resolveLinkByTables(String propertyName, ITableMetaData table, IDatabaseMetaData database, ILinkMetaData link) {
        var member = getMemberByTypeAndName(table.getEntityType(), propertyName);
        var relatedEntityType = member.getElementType();
        var relatedTable = getTableByType(database, relatedEntityType);
        var links = database.getLinksByTables(table, relatedTable);
        if (links == null || links.length == 0) {
            throw new IllegalArgumentException("No Link found for '" + table.getEntityType().getName() + "." + propertyName + "'");
        } else if (links.length == 1) {
            link = links[0];
            return link;
        } else {
            throw new IllegalArgumentException(
                    "Unconfigured Link for '" + table.getEntityType().getName() + "." + propertyName + "' not uniquely idenfifiable. Please provide the name in the orm.xml file.");
        }
    }

    private String findLinkNameLocal(ITableMetaData table, String memberName, String joinTableName, RelationConfigLegathy relationConfig, ITableMetaData table2, DatabaseMetaData database) {
        String linkName;
        if (joinTableName == null) {
            linkName = resolveLinkDynamically(database, memberName, relationConfig.getConstraintName(), table, table2);
        } else {
            var useLinkTable = !table.getName().equalsIgnoreCase(joinTableName) && !table2.getName().equalsIgnoreCase(joinTableName);
            if (!useLinkTable) {
                linkName = mapDataTableWithLink(database, table, table2, relationConfig, false);
                if (database.getLinkByName(linkName) == null) {
                    var linkNameRetry = mapDataTableWithLink(database, table, table2, relationConfig, true);
                    if (database.getLinkByName(linkNameRetry) != null) {
                        linkName = linkNameRetry;
                    }
                }
            } else {
                linkName = joinTableName;
            }
        }
        return linkName;
    }

    private String createFkLinkToExternalEntity(Connection connection, DatabaseMetaData database, ITableMetaData table, String fromFieldName, String toFieldName, Class<?> linkedEntityType,
            String toAttributeName, Member toMember, boolean toMany) {
        var fromField = table.getFieldByName(fromFieldName);
        if (fromField == null) {
            throw new IllegalArgumentException(
                    "No field found with name '" + fromFieldName + "' on table '" + table + "'. Available field names: '" + Arrays.toString(table.getAllFields().toArray()) + "'");
        }
        IFieldMetaData toField = null;
        if (toMany && toFieldName != null && !toFieldName.isEmpty()) {
            toField = table.getFieldByName(toFieldName);
        }
        var linkName = database.createForeignKeyLinkName(table.getName(), fromFieldName, linkedEntityType.getSimpleName(), toAttributeName);
        buildAndMapLink(connection, database, linkName, table, fromField, toField, toMember);
        return linkName;
    }

    private ILinkMetaData findLinkForRelation(String propertyName, String linkName, ITableMetaData table, IDatabaseMetaData database) {
        ILinkMetaData link = null;
        if (!linkName.isEmpty()) {
            link = database.getLinkByDefiningName(linkName);
            if (link == null) {
                link = database.getLinkByDefiningName(linkName.toUpperCase());
                if (link == null) {
                    link = database.getLinkByName(linkName);
                }
            }
        } else {
            link = resolveLinkByTables(propertyName, table, database, link);
        }
        return link;
    }

    protected String resolveLinkDynamically(IDatabaseMetaData database, String memberName, String constraintName, ITableMetaData table, ITableMetaData table2) {
        if (constraintName == null) {
            return null;
        }
        var links = database.getLinks();
        for (int a = links.size(); a-- > 0; ) {
            var link = links.get(a);
            if (!(link instanceof SqlLinkMetaData)) {
                continue;
            }
            var constraintNameOfLink = ((SqlLinkMetaData) link).getConstraintName();
            if (constraintNameOfLink == null) {
                var linkForward = (DirectedLinkMetaData) link.getDirectedLink();
                var linkReverse = (DirectedLinkMetaData) link.getReverseDirectedLink();
                if (constraintName.equalsIgnoreCase(linkForward.getConstraintName())) {
                    return linkForward.getName();
                }
                if (constraintName.equalsIgnoreCase(linkReverse.getConstraintName())) {
                    return linkReverse.getName();
                }
            }
            if (constraintName.equalsIgnoreCase(constraintNameOfLink)) {
                return link.getName();
            }
        }
        return null;
    }

    protected String mapDataTableWithLink(DatabaseMetaData database, ITableMetaData table, ITableMetaData table2, RelationConfigLegathy relationConfig, boolean reverse) {
        var joinTableName = getFqJoinTableName(table, relationConfig);
        ITableMetaData fromTable, toTable;
        if (table.getName().equalsIgnoreCase(joinTableName)) {
            fromTable = table;
            toTable = table2;
        } else {
            fromTable = table2;
            toTable = table;
        }
        if (reverse) {
            ITableMetaData tempTable = fromTable;
            fromTable = toTable;
            toTable = tempTable;
        }
        var fromTableName = fromTable.getName();
        var fromFieldName = relationConfig.getFromFieldName();
        var toTableName = toTable.getName();
        var toFieldName = relationConfig.getToFieldName();
        if (reverse) {
            String tempFieldName = fromFieldName;
            fromFieldName = toFieldName;
            toFieldName = tempFieldName;
        }
        mapFieldToMember(table, joinTableName, fromFieldName, relationConfig.getName());
        var linkName = database.createForeignKeyLinkName(fromTableName, fromFieldName, toTableName, toFieldName);
        return linkName;
    }

    protected void mapFieldToMember(ITableMetaData table, String joinTableName, String fieldName, String memberName) {
        if (table.getName().equalsIgnoreCase(joinTableName) && table.getFieldByName(fieldName).getMember() == null) {
            table.mapField(fieldName, memberName);
            if (log.isDebugEnabled()) {
                log.debug("Configuring member '" + table.getEntityType().getName() + "." + memberName + "' for database field '" + table.getName() + "." + fieldName + "'");
            }
        }
    }

    protected void setCascadeValuesAndMapLink(ITableMetaData table, ILinkMetaData link, String memberName, boolean doDelete, boolean mayDelete) {
        if (table.equals(link.getFromTable())) {
            ((DirectedLinkMetaData) link.getDirectedLink()).setCascadeDelete(doDelete);
            ((DirectedLinkMetaData) link.getReverseDirectedLink()).setCascadeDelete(mayDelete);
            if (link.getDirectedLink().getMember() == null) {
                table.mapLink(link.getDirectedLink().getName(), memberName);
            }
        } else {
            ((DirectedLinkMetaData) link.getDirectedLink()).setCascadeDelete(mayDelete);
            ((DirectedLinkMetaData) link.getReverseDirectedLink()).setCascadeDelete(doDelete);
            if (link.getReverseDirectedLink().getMember() == null) {
                table.mapLink(link.getReverseDirectedLink().getName(), memberName);
            }
        }
    }

    protected void addToMemberToPreBuildLink(IDatabaseMetaData database, String linkName, Member toMember) {
        var link = (SqlLinkMetaData) database.getLinkByName(linkName);

        var directed = (DirectedExternalLinkMetaData) link.getDirectedLink();
        directed.setEntityMetaDataProvider(entityMetaDataProvider);
        directed.setToMember(toMember);
        directed.afterPropertiesSet();

        var reverse = (DirectedExternalLinkMetaData) link.getReverseDirectedLink();
        reverse.setEntityMetaDataProvider(entityMetaDataProvider);
        reverse.setFromMember(toMember);
        reverse.afterPropertiesSet();

        ((IConfigurableDatabaseMetaData) database).mapLink(link);
    }

    protected void handleExternalEntities() {
        var tlObjectCollector = objectCollector.getCurrent();
        var debugSb = tlObjectCollector.create(StringBuilder.class);
        try {
            for (var ormConfigGroup : ormConfigGroups.getExtensionsShared()) {
                for (var entityConfig : ormConfigGroup.getExternalEntityConfigs()) {
                    var entityType = entityConfig.getEntityType();
                    var realType = entityConfig.getRealType();

                    var metaData = new EntityMetaData();
                    metaData.setEntityType(entityType);
                    metaData.setRealType(realType);
                    metaData.setLocalEntity(false);

                    entityMetaDataReader.addMembers(metaData, entityConfig);

                    if (metaData.getIdMember() == null) {
                        throw new IllegalArgumentException("ID attribute missing in configuration for external entity '" + entityType.getName() + "'");
                    }
                    if (entityConfig.isVersionRequired() && metaData.getVersionMember() == null) {
                        throw new IllegalArgumentException("Version attribute missing in configuration for external entity '" + entityType.getName() + "'");
                    }

                    synchronized (entityMetaDataExtendable) {
                        if (entityMetaDataProvider.getMetaData(metaData.getEntityType(), true) == null) {
                            entityMetaDataExtendable.registerEntityMetaData(metaData);
                            registeredMetaDatas.add(metaData);
                        }
                    }
                    if (log.isDebugEnabled()) {
                        debugSb.setLength(0);

                        debugSb.append("Mapped external entity '").append(metaData.getEntityType().getName()).append("' with members ID ('").append(metaData.getIdMember().getName());
                        if (entityConfig.isVersionRequired()) {
                            debugSb.append("'), version ('").append(metaData.getVersionMember().getName()).append("')");
                        } else {
                            debugSb.append("'), no version");
                        }
                        debugPrintMembers(debugSb, metaData.getAlternateIdMembers(), "alternate IDs");
                        debugPrintMembers(debugSb, metaData.getPrimitiveMembers(), "primitives");
                        debugPrintMembers(debugSb, metaData.getRelationMembers(), "relations");

                        log.debug(debugSb.toString());
                    }
                }
            }
        } finally {
            tlObjectCollector.dispose(debugSb);
        }
    }

    protected void debugPrintMembers(StringBuilder debugSb, Member[] toPrint, String membersName) {
        if (toPrint.length == 0) {
            debugSb.append(", no ").append(membersName);
        } else {
            debugSb.append(", ").append(membersName).append(" ('");
            for (int j = 0; j < toPrint.length; j++) {
                var memberToPrint = toPrint[j];
                if (j > 0) {
                    debugSb.append("', '");
                }
                debugSb.append(memberToPrint.getName());
            }
            debugSb.append("')");
        }
    }

    protected Member getMemberByTypeAndName(Class<?> entityType, String memberName) {
        var member = memberTypeProvider.getMember(entityType, memberName);
        if (member == null) {
            throw new IllegalArgumentException("No entity member found for name '" + memberName + "' on entity '" + entityType.getName() + "'");
        }
        return member;
    }

    protected SqlLinkMetaData buildAndMapLink(Connection connection, IConfigurableDatabaseMetaData confDatabase, String linkName, ITableMetaData fromTable, IFieldMetaData fromField,
            IFieldMetaData toField, Member member) {
        var link = new SqlLinkMetaData();
        boolean nullable = nullable = confDatabase.isFieldNullable(connection, fromField);

        if (!fromField.isAlternateId()) {
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
    public void mapFields(IOrmConfigGroup ormConfigGroup) {
        List<ITableMetaData> tables = mapFieldsIntern(databaseMetaData, ormConfigGroup);
        List<IDatabaseMappedListener> databaseMappedListeners = serviceContext.getObjects(IDatabaseMappedListener.class);

        for (ITableMetaData table : tables) {
            databaseMetaData.handleTable(table);
            for (IDatabaseMappedListener listener : databaseMappedListeners) {
                listener.newTableMetaData(table);
            }
        }
    }

    @Override
    public void registerOrmConfigGroup(IOrmConfigGroup ormConfigGroup) {
        ormConfigGroups.register(ormConfigGroup);
    }

    @Override
    public void unregisterOrmConfigGroup(IOrmConfigGroup ormConfigGroup) {
        ormConfigGroups.unregister(ormConfigGroup);
    }
}
