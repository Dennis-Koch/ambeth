package com.koch.ambeth.merge.server;

/*-
 * #%L
 * jambeth-merge-server
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

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IEntityMetaDataExtendable;
import com.koch.ambeth.merge.IMergeServiceExtension;
import com.koch.ambeth.merge.IMergeServiceExtensionExtendable;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.persistence.DirectedLinkMetaData;
import com.koch.ambeth.persistence.FieldMetaData;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.database.IDatabaseMappedListener;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IEntityMetaDataRefresher;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.IntermediatePrimitiveMember;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class DatabaseToEntityMetaData implements IDatabaseMappedListener, IDisposableBean {
    public static final String P_PersistenceCacheRetriever = "PersistenceCacheRetriever";
    public static final String P_PersistenceMergeServiceExtension = "PersistenceMergeServiceExtension";
    protected final ClassExtendableContainer<IEntityMetaData> registeredMetaDatas = new ClassExtendableContainer<>("metaData", "entityType");
    protected final ArrayList<IEntityMetaData> handledMetaDatas = new ArrayList<>();
    @Autowired
    protected ICacheRetrieverExtendable cacheRetrieverExtendable;
    @Autowired
    protected ICompositeIdFactory compositeIdFactory;
    @Autowired
    protected IEntityFactory entityFactory;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IEntityMetaDataExtendable entityMetaDataExtendable;
    @Autowired
    protected IEntityMetaDataRefresher entityMetaDataRefresher;
    @Autowired
    protected IMergeServiceExtensionExtendable mergeServiceExtensionExtendable;
    @Autowired
    protected ICacheRetriever persistenceCacheRetriever;
    @Autowired
    protected IMergeServiceExtension persistenceMergeServiceExtension;
    protected boolean firstMapping = true;
    @LogInstance
    private ILogger log;

    protected void addValidMember(ISet<Member> set, Member member) {
        if (member == null) {
            return;
        }
        set.add(member);
    }

    protected void checkRelationMember(EntityMetaData metaData, RelationMember[] relationsNew) {
        var relationsOld = metaData.getRelationMembers();
        if (!Arrays.equals(relationsOld, relationsNew)) {
            throw new IllegalStateException(
                    "Relation member arrays for entity '" + metaData.getEntityType().getName() + "' do not match. From object model: " + Arrays.deepToString(relationsOld) + ", from database model: " +
                            Arrays.deepToString(relationsNew));
        }
    }

    @Override
    public synchronized void databaseMapped(IDatabaseMetaData database) {
        if (!firstMapping) {
            return;
        }
        firstMapping = false;
        var alreadyHandledFields = new HashSet<IFieldMetaData>();
        var newMetaDatas = new ArrayList<IEntityMetaData>();
        var newRegisteredMetaDatas = new ArrayList<IEntityMetaData>();
        for (var table : database.getTables()) {
            mapTable(table, newMetaDatas, newRegisteredMetaDatas, alreadyHandledFields);
        }
        for (var newRegisteredMetaData : newRegisteredMetaDatas) {
            entityMetaDataExtendable.registerEntityMetaData(newRegisteredMetaData);
            registeredMetaDatas.register(newRegisteredMetaData, newRegisteredMetaData.getEntityType());
        }
        for (var newMetaData : newMetaDatas) {
            var entityType = newMetaData.getEntityType();
            mergeServiceExtensionExtendable.registerMergeServiceExtension(persistenceMergeServiceExtension, entityType);
            cacheRetrieverExtendable.registerCacheRetriever(persistenceCacheRetriever, entityType);
            handledMetaDatas.add(newMetaData);
        }
    }

    @Override
    public void destroy() throws Throwable {
        for (int a = handledMetaDatas.size(); a-- > 0; ) {
            var handledMetaData = handledMetaDatas.get(a);
            cacheRetrieverExtendable.unregisterCacheRetriever(persistenceCacheRetriever, handledMetaData.getEntityType());
            mergeServiceExtensionExtendable.unregisterMergeServiceExtension(persistenceMergeServiceExtension, handledMetaData.getEntityType());
        }
        var extensions = registeredMetaDatas.getExtensions();
        for (var entry : extensions) {
            var registeredMetaData = entry.getValue();
            registeredMetaDatas.unregister(registeredMetaData, entry.getKey());
            entityMetaDataExtendable.unregisterEntityMetaData(registeredMetaData);
        }
    }

    protected PrimitiveMember findPrimitiveMember(PrimitiveMember memberToFind, ISet<PrimitiveMember> members) {
        if (memberToFind == null) {
            return null;
        }
        for (var member : members) {
            if (memberToFind.getName().equals(member.getName())) {
                return (PrimitiveMember) member;
            }
        }
        return memberToFind;
    }

    protected boolean isMemberOnFieldBetter(Class<?> entityType, Member existingMember, IFieldMetaData newMemberField) {
        if (newMemberField == null) {
            return false;
        }
        var member = newMemberField.getMember();
        if (member == null) {
            ((FieldMetaData) newMemberField).setMember(existingMember);
            return false;
        }
        if (existingMember == null) {
            return true;
        }
        if (existingMember == member || existingMember.getName().equals(member.getName())) {
            return false;
        }
        throw new IllegalStateException("Inconsistent metadata configuration on member '" + existingMember.getName() + "' of entity '" + entityType.getName() + "'");
    }

    protected boolean isMemberOnMetaDataBetter(Class<?> entityType, Member existingMember, IFieldMetaData newMemberField) {
        if (newMemberField == null) {
            return false;
        }
        var member = newMemberField.getMember();
        if (member == null) {
            return false;
        }
        if (existingMember == null) {
            return false;
        }
        if (existingMember == member || existingMember.getName().equals(member.getName())) {
            return false;
        }
        throw new IllegalStateException("Inconsistent metadata configuration on member '" + existingMember.getName() + "' of entity '" + entityType.getName() + "'");
    }

    protected void mapTable(ITableMetaData table, List<IEntityMetaData> newMetaDatas, List<IEntityMetaData> newRegisteredMetaDatas, HashSet<IFieldMetaData> alreadyHandledFields) {
        var entityType = table.getEntityType();
        if (entityType == null || table.isArchive()) {
            return;
        }
        var metaData = (EntityMetaData) entityMetaDataProvider.getMetaData(entityType, true);
        if (metaData == null) {
            metaData = new EntityMetaData();
            metaData.setEntityType(entityType);
            newRegisteredMetaDatas.add(metaData);
        }
        var fMetaData = metaData;
        // metaData.setEntityType(entityType);
        // metaData.setRealType(null);
        var idFields = table.getIdFields();
        if (idFields.length > 1) {
            var idMember = metaData.getIdMember();
            if (!(idMember instanceof CompositeIdMember)) {
                throw new IllegalStateException("Not yet handled");
            }
            var members = ((CompositeIdMember) idMember).getMembers();
            for (int a = members.length; a-- > 0; ) {
                if (isMemberOnFieldBetter(entityType, members[a], idFields[a])) {
                    metaData.setIdMember((PrimitiveMember) idFields[a].getMember());
                }
            }
        } else if (isMemberOnFieldBetter(entityType, metaData.getIdMember(), table.getIdField())) {
            metaData.setIdMember((PrimitiveMember) table.getIdField().getMember());
        }
        alreadyHandledFields.addAll(idFields);
        var versionField = table.getVersionField();
        if (versionField != null) {
            alreadyHandledFields.add(versionField);
            if (isMemberOnFieldBetter(entityType, metaData.getVersionMember(), versionField)) {
                metaData.setVersionMember((PrimitiveMember) versionField.getMember());
            }
        }

        if (table.getCreatedOnField() != null) {
            if (isMemberOnFieldBetter(entityType, metaData.getCreatedOnMember(), table.getCreatedOnField())) {
                metaData.setCreatedOnMember((PrimitiveMember) table.getCreatedOnField().getMember());
            }
        }
        if (table.getCreatedByField() != null) {
            if (isMemberOnFieldBetter(entityType, metaData.getCreatedByMember(), table.getCreatedByField())) {
                metaData.setCreatedByMember((PrimitiveMember) table.getCreatedByField().getMember());
            }
        }
        if (table.getUpdatedOnField() != null) {
            if (isMemberOnFieldBetter(entityType, metaData.getUpdatedOnMember(), table.getUpdatedOnField())) {
                metaData.setUpdatedOnMember((PrimitiveMember) table.getUpdatedOnField().getMember());
            }
        }
        if (table.getUpdatedByField() != null) {
            if (isMemberOnFieldBetter(entityType, metaData.getUpdatedByMember(), table.getUpdatedByField())) {
                metaData.setUpdatedByMember((PrimitiveMember) table.getUpdatedByField().getMember());
            }
        }

        var fulltextMembers = new ArrayList<Member>();
        var primitiveMembers = new HashSet<PrimitiveMember>();
        var relationMembers = new HashSet<RelationMember>();
        var memberToFieldOrLinkMap = new HashMap<Member, Object>();

        // IField[] fulltextFieldsContains = table.getFulltextFieldsContains();
        // for (int a = 0; a < fulltextFieldsContains.length; a++)
        // {
        // IField field = fulltextFieldsContains[a];
        // ITypeInfoItem member = field.getMember();
        // if (member != null)
        // {
        // fulltextMembers.add(member);
        // }
        // }
        // IField[] fulltextFieldsCatsearch = table.getFulltextFieldsCatsearch();
        // for (int a = 0; a < fulltextFieldsCatsearch.length; a++)
        // {
        // IField field = fulltextFieldsCatsearch[a];
        var fulltextFields = table.getFulltextFields();
        for (int a = 0; a < fulltextFields.size(); a++) {
            var field = fulltextFields.get(a);
            var member = field.getMember();
            if (member != null) {
                fulltextMembers.add(member);
                memberToFieldOrLinkMap.put(member, field);
            }
        }

        var fields = table.getPrimitiveFields();
        for (int a = 0; a < fields.size(); a++) {
            var field = fields.get(a);

            var member = field.getMember();
            if (member == null) {
                continue;
            }
            memberToFieldOrLinkMap.put(member, field);
            if (!alreadyHandledFields.contains(field)) {
                if (member instanceof RelationMember) {
                    throw new IllegalStateException("The given member '" + member +
                            "' is neither mapped as a primitive nor as a relation explicitly or implicitly. In most cases this is meant as a relation but the related property type could not be " +
                            "resolved as a valid entity. Please check whether you have mapped the type '" + member.getElementType().getName() + "' correctly as an entity");
                }
                primitiveMembers.add((PrimitiveMember) member);
            }
        }

        var links = table.getLinks();
        for (int a = 0; a < links.size(); a++) {
            var link = links.get(a);
            var member = link.getMember();
            if (member == null) {
                continue;
            }
            memberToFieldOrLinkMap.put(member, link);
            var otherType = link.getToEntityType();
            relationMembers.add(member);
            if (link.getReverseLink().isCascadeDelete()) {
                metaData.addCascadeDeleteType(otherType);
            }
        }

        var relationMembersList = relationMembers.toList();
        Collections.sort(relationMembersList, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        {
            var existingPrimitiveMembers = metaData.getPrimitiveMembers();
            if (existingPrimitiveMembers != null) {
                for (var primitiveMember : existingPrimitiveMembers) {
                    if (primitiveMember.isTransient()) {
                        // ensure that transient members are not dropped because no persisted column
                        // has been
                        // found in the database table
                        primitiveMembers.add(primitiveMember);
                    }
                }
            }
        }

        // Order of setter calls is important
        metaData.setCreatedByMember(findPrimitiveMember(metaData.getCreatedByMember(), primitiveMembers));
        metaData.setCreatedOnMember(findPrimitiveMember(metaData.getCreatedOnMember(), primitiveMembers));
        metaData.setUpdatedByMember(findPrimitiveMember(metaData.getUpdatedByMember(), primitiveMembers));
        metaData.setUpdatedOnMember(findPrimitiveMember(metaData.getUpdatedOnMember(), primitiveMembers));

        for (var existingRelationMember : metaData.getRelationMembers()) {
            relationMembers.add(existingRelationMember);
        }
        var primitives = primitiveMembers.toArray(PrimitiveMember[]::new);
        var fulltexts = fulltextMembers.toArray(PrimitiveMember[]::new);
        var relations = relationMembers.toArray(RelationMember[]::new);
        Arrays.sort(primitives);
        Arrays.sort(fulltexts);
        Arrays.sort(relations);
        metaData.setPrimitiveMembers(primitives);
        metaData.setFulltextMembers(fulltexts);

        var alternateIdsFields = table.getAlternateIdsFields();
        var alternateIdsMembers = Stream.of(alternateIdsFields)
                                        .map(alternateIdFields -> Stream.of(alternateIdFields).map(IFieldMetaData::getMember).toArray(PrimitiveMember[]::new))
                                        .map(alternateIdMembers -> compositeIdFactory.createCompositeIdMember(fMetaData, alternateIdMembers))
                                        .toArray(PrimitiveMember[]::new);

        metaData.setAlternateIdMembers(alternateIdsMembers);
        // FIXME To many tests fail with this line
        // checkRelationMember(metaData, relations);
        metaData.setRelationMembers(relations);
        metaData.setEnhancedType(null);

        metaData.setIdMember(curateIdMember(metaData.getIdMember(), table.getIdFields()));
        var alternateIdMembers = metaData.getAlternateIdMembers();
        for (int idIndex = alternateIdMembers.length; idIndex-- > 0; ) {
            alternateIdsMembers[idIndex] = curateIdMember(alternateIdMembers[idIndex], table.getIdFieldsByAlternateIdIndex(idIndex));
        }

        entityMetaDataRefresher.refreshMembers(metaData);
        var allRefreshedMembers = new HashSet<Member>();
        addValidMember(allRefreshedMembers, metaData.getIdMember());
        addValidMember(allRefreshedMembers, metaData.getVersionMember());
        allRefreshedMembers.addAll(metaData.getPrimitiveMembers());
        allRefreshedMembers.addAll(metaData.getRelationMembers());

        for (var entry : memberToFieldOrLinkMap) {
            var member = entry.getKey();
            var refreshedMember = allRefreshedMembers.get(member);
            if (refreshedMember == null) {
                throw new IllegalStateException("Member '" + member.getName() + "' has not been refreshed");
            }
            var value = entry.getValue();
            if (value instanceof FieldMetaData) {
                ((FieldMetaData) value).setMember(refreshedMember);
            } else if (value instanceof DirectedLinkMetaData) {
                ((DirectedLinkMetaData) value).setMember((RelationMember) refreshedMember);
            }
        }
        newMetaDatas.add(metaData);
    }

    protected PrimitiveMember curateIdMember(PrimitiveMember idMember, IFieldMetaData[] idFields) {
        if (!Object.class.equals(idMember.getElementType())) {
            return idMember;
        }
        if (idFields.length > 1) {
            throw new IllegalStateException("Composite id not supported if entity metadata only configures 'Object.class' als id type: " + idMember);
        }
        var elementType = idFields[0].getFieldType();
        var realType = Object.class.equals(idMember.getRealType()) ? elementType : idMember.getRealType();
        return new IntermediatePrimitiveMember(idMember.getDeclaringType(), idMember.getEntityType(), realType, elementType, idMember.getName(), idMember.getAnnotations());
    }

    @Override
    public void newTableMetaData(ITableMetaData newTable) {

        var alreadyHandledFields = new HashSet<IFieldMetaData>();
        var newMetaDatas = new ArrayList<IEntityMetaData>();
        var newRegisteredMetaDatas = new ArrayList<IEntityMetaData>();

        mapTable(newTable, newMetaDatas, newRegisteredMetaDatas, alreadyHandledFields);

        for (int a = 0, size = newRegisteredMetaDatas.size(); a < size; a++) {
            var newRegisteredMetaData = newRegisteredMetaDatas.get(a);
            entityMetaDataExtendable.registerEntityMetaData(newRegisteredMetaData);
            registeredMetaDatas.register(newRegisteredMetaData, newRegisteredMetaData.getEntityType());
        }
        for (int a = 0, size = newMetaDatas.size(); a < size; a++) {
            var newMetaData = newMetaDatas.get(a);
            var entityType = newMetaData.getEntityType();
            mergeServiceExtensionExtendable.registerMergeServiceExtension(persistenceMergeServiceExtension, entityType);
            cacheRetrieverExtendable.registerCacheRetriever(persistenceCacheRetriever, entityType);
            handledMetaDatas.add(newMetaData);
        }
    }
}
