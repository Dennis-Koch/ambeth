package com.koch.ambeth.merge.converter;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.metadata.IIntermediateMemberTypeProvider;
import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.merge.transfer.EntityMetaDataTransfer;
import com.koch.ambeth.service.metadata.IntermediatePrimitiveMember;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EntityMetaDataConverter implements IDedicatedConverter {
    protected static final Pattern memberPathSplitPattern = Pattern.compile("\\.");
    private static final String[] EMPTY_STRINGS = new String[0];

    private static final String[][] EMPTY_STRINGS_ARRAY = new String[0][0];

    @Autowired
    protected ICacheModification cacheModification;
    @Autowired
    protected IEntityFactory entityFactory;
    @Autowired
    protected IIntermediateMemberTypeProvider intermediateMemberTypeProvider;
    @Autowired
    protected IProxyHelper proxyHelper;
    @LogInstance
    private ILogger log;

    @Override
    public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) {
        if (sourceType.isAssignableFrom(EntityMetaData.class)) {
            EntityMetaData source = (EntityMetaData) value;

            EntityMetaDataTransfer target = new EntityMetaDataTransfer();
            target.setEntityType(source.getEntityType());
            target.setIdMemberName(getNameOfMember(source.getIdMember()));
            target.setVersionMemberName(getNameOfMember(source.getVersionMember()));
            target.setCreatedByMemberName(getNameOfMember(source.getCreatedByMember()));
            target.setCreatedOnMemberName(getNameOfMember(source.getCreatedOnMember()));
            target.setUpdatedByMemberName(getNameOfMember(source.getUpdatedByMember()));
            target.setUpdatedOnMemberName(getNameOfMember(source.getUpdatedOnMember()));
            target.setAlternateIdMemberNames(getNamesOfMembers(source.getAlternateIdMembers()));
            target.setPrimitiveMemberNames(getNamesOfMembers(source.getPrimitiveMembers()));
            target.setRelationMemberNames(getNamesOfMembers(source.getRelationMembers()));
            target.setTypesRelatingToThis(source.getTypesRelatingToThis());
            target.setTypesToCascadeDelete(source.getCascadeDeleteTypes().toArray(new Class<?>[source.getCascadeDeleteTypes().size()]));

            Member[] primitiveMembers = source.getPrimitiveMembers();
            RelationMember[] relationMembers = source.getRelationMembers();
            List<String> mergeRelevantNames = new ArrayList<>();
            for (int a = primitiveMembers.length; a-- > 0; ) {
                Member member = primitiveMembers[a];
                if (source.isMergeRelevant(member)) {
                    mergeRelevantNames.add(getNameOfMember(member));
                }
            }
            for (int a = relationMembers.length; a-- > 0; ) {
                RelationMember member = relationMembers[a];
                if (source.isMergeRelevant(member)) {
                    mergeRelevantNames.add(getNameOfMember(member));
                }
            }
            target.setMergeRelevantNames(ListUtil.toArray(String.class, mergeRelevantNames));
            return target;
        } else if (sourceType.isAssignableFrom(EntityMetaDataTransfer.class)) {
            EntityMetaDataTransfer source = (EntityMetaDataTransfer) value;

            HashMap<String, Member> nameToMemberDict = new HashMap<>();

            EntityMetaData target = new EntityMetaData();
            Class<?> entityType = source.getEntityType();

            if (entityType == null) {
                // entity class is not known in the current class loader
                if (log.isDebugEnabled()) {
                    log.debug("Received metadata for an unknown entity type");
                }
                return null;
            }
            Class<?> realType = proxyHelper.getRealType(entityType);
            target.setEntityType(entityType);
            target.setRealType(realType);
            target.setIdMember(getPrimitiveMember(entityType, source.getIdMemberName(), nameToMemberDict));
            target.setVersionMember(getPrimitiveMember(entityType, source.getVersionMemberName(), nameToMemberDict));
            target.setCreatedByMember(getPrimitiveMember(entityType, source.getCreatedByMemberName(), nameToMemberDict));
            target.setCreatedOnMember(getPrimitiveMember(entityType, source.getCreatedOnMemberName(), nameToMemberDict));
            target.setUpdatedByMember(getPrimitiveMember(entityType, source.getUpdatedByMemberName(), nameToMemberDict));
            target.setUpdatedOnMember(getPrimitiveMember(entityType, source.getUpdatedOnMemberName(), nameToMemberDict));
            target.setPrimitiveMembers(getPrimitiveMembers(entityType, source.getPrimitiveMemberNames(), nameToMemberDict));
            target.setAlternateIdMembers(getPrimitiveMembers(entityType, source.getAlternateIdMemberNames(), nameToMemberDict));
            target.setRelationMembers(getRelationMembers(entityType, source.getRelationMemberNames(), nameToMemberDict));
            target.setTypesRelatingToThis(source.getTypesRelatingToThis());
            Class<?>[] typesToCascadeDelete = source.getTypesToCascadeDelete();
            for (int a = 0, size = typesToCascadeDelete.length; a < size; a++) {
                target.getCascadeDeleteTypes().add(typesToCascadeDelete[a]);
            }
            String[] mergeRelevantNames = source.getMergeRelevantNames();
            if (mergeRelevantNames != null) {
                for (int a = mergeRelevantNames.length; a-- > 0; ) {
                    Member resolvedMember = nameToMemberDict.get(mergeRelevantNames[a]);
                    target.setMergeRelevant(resolvedMember, true);
                }
            }
            setMergeRelevant(target, target.getCreatedByMember(), false);
            setMergeRelevant(target, target.getCreatedOnMember(), false);
            setMergeRelevant(target, target.getUpdatedByMember(), false);
            setMergeRelevant(target, target.getUpdatedOnMember(), false);
            setMergeRelevant(target, target.getIdMember(), false);
            setMergeRelevant(target, target.getVersionMember(), false);
            return target;
        }
        throw new IllegalStateException("Source of type " + sourceType.getName() + " not supported");
    }

    protected void setMergeRelevant(EntityMetaData metaData, Member member, boolean value) {
        if (member != null) {
            metaData.setMergeRelevant(member, value);
        }
    }

    protected IntermediatePrimitiveMember getPrimitiveMember(Class<?> entityType, String memberName, Map<String, Member> nameToMemberDict) {
        if (memberName == null) {
            return null;
        }
        IntermediatePrimitiveMember member = (IntermediatePrimitiveMember) nameToMemberDict.get(memberName);
        if (member != null) {
            return member;
        }
        member = intermediateMemberTypeProvider.getIntermediatePrimitiveMember(entityType, memberName);
        if (member == null) {
            throw new RuntimeException("No member with name '" + memberName + "' found on entity type '" + entityType.getName() + "'");
        }
        nameToMemberDict.put(memberName, member);
        return member;
    }

    protected RelationMember getRelationMember(Class<?> entityType, String memberName, Map<String, Member> nameToMemberDict) {
        RelationMember member = (RelationMember) nameToMemberDict.get(memberName);
        if (member != null) {
            return member;
        }
        member = intermediateMemberTypeProvider.getIntermediateRelationMember(entityType, memberName);
        if (member == null) {
            throw new RuntimeException("No member with name '" + memberName + "' found on entity type '" + entityType.getName() + "'");
        }
        nameToMemberDict.put(memberName, member);
        return member;
    }

    protected PrimitiveMember[] getPrimitiveMembers(Class<?> entityType, String[] memberNames, Map<String, Member> nameToMemberDict) {
        if (memberNames == null || memberNames.length == 0) {
            return EntityMetaData.emptyPrimitiveMembers;
        }
        var members = new PrimitiveMember[memberNames.length];
        for (int a = memberNames.length; a-- > 0; ) {
            members[a] = getPrimitiveMember(entityType, memberNames[a], nameToMemberDict);
        }
        return members;
    }

    protected RelationMember[] getRelationMembers(Class<?> entityType, String[] memberNames, Map<String, Member> nameToMemberDict) {
        if (memberNames == null || memberNames.length == 0) {
            return EntityMetaData.emptyRelationMembers;
        }
        var members = new RelationMember[memberNames.length];
        for (int a = memberNames.length; a-- > 0; ) {
            members[a] = getRelationMember(entityType, memberNames[a], nameToMemberDict);
        }
        return members;
    }

    protected String getNameOfMember(Member member) {
        if (member == null) {
            return null;
        }
        return member.getName();
    }

    protected String[] getNamesOfMembers(Member[] members) {
        if (members == null || members.length == 0) {
            return EMPTY_STRINGS;
        }
        var names = new String[members.length];
        for (int a = members.length; a-- > 0; ) {
            names[a] = getNameOfMember(members[a]);
        }
        return names;
    }
}
