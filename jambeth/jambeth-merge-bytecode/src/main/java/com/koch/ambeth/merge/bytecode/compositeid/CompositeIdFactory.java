package com.koch.ambeth.merge.bytecode.compositeid;

/*-
 * #%L
 * jambeth-merge-bytecode
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.util.IConversionHelper;
import lombok.SneakyThrows;

public class CompositeIdFactory implements ICompositeIdFactory, IInitializingBean {
    @Autowired(optional = true)
    protected IBytecodeEnhancer bytecodeEnhancer;
    @Autowired
    protected IConversionHelper conversionHelper;
    @Autowired
    protected IMemberTypeProvider memberTypeProvider;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        if (bytecodeEnhancer == null) {
            log.debug("No bytecodeEnhancer specified: Composite ID feature deactivated");
        }
    }

    @Override
    public PrimitiveMember createCompositeIdMember(IEntityMetaData metaData, PrimitiveMember[] idMembers) {
        return createCompositeIdMember(metaData.getEntityType(), idMembers);
    }

    @Override
    public PrimitiveMember createCompositeIdMember(Class<?> entityType, PrimitiveMember[] idMembers) {
        if (idMembers.length == 1) {
            return idMembers[0];
        }
        if (bytecodeEnhancer == null) {
            throw new UnsupportedOperationException("No bytecodeEnhancer specified");
        }
        var nameSB = new StringBuilder();
        // order does matter here
        for (int a = 0, size = idMembers.length; a < size; a++) {
            var name = idMembers[a].getName();
            if (a > 0) {
                nameSB.append('&');
            }
            nameSB.append(name);
        }
        var compositeIdType = bytecodeEnhancer.getEnhancedType(Object.class, new CompositeIdEnhancementHint(idMembers));
        return new CompositeIdMember(entityType, compositeIdType, nameSB.toString(), idMembers, memberTypeProvider);
    }

    @SneakyThrows
    @Override
    public Object createCompositeId(IEntityMetaData metaData, PrimitiveMember compositeIdMember, Object... ids) {
        if (ids.length == 1) {
            var id = ids[0];
            return conversionHelper.convertValueToType(compositeIdMember.getRealType(), id);
        }
        var conversionHelper = this.conversionHelper;
        var cIdTypeInfoItem = (CompositeIdMember) compositeIdMember;
        var members = cIdTypeInfoItem.getMembers();
        for (int a = ids.length; a-- > 0; ) {
            var id = ids[a];
            var convertedId = conversionHelper.convertValueToType(members[a].getRealType(), id);
            if (convertedId != id) {
                ids[a] = convertedId;
            }
        }
        return cIdTypeInfoItem.getRealTypeConstructorAccess().newInstance(ids);
    }

    @SneakyThrows
    @Override
    public Object createCompositeId(IEntityMetaData metaData, int idIndex, Object... ids) {
        var idMember = metaData.getIdMemberByIdIndex(idIndex);
        if (idMember instanceof CompositeIdMember compositeIdMember) {
            var conversionHelper = this.conversionHelper;
            var members = compositeIdMember.getMembers();
            for (int a = members.length; a-- > 0; ) {
                var id = ids[a];
                var convertedId = conversionHelper.convertValueToType(members[a].getRealType(), id);
                if (convertedId != id) {
                    ids[a] = convertedId;
                }
            }
            return compositeIdMember.getRealTypeConstructorAccess().newInstance(ids);
        }
        var id = ids[0];
        return conversionHelper.convertValueToType(idMember.getRealType(), id);
    }

    @Override
    public Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives) {
        var alternateIdMemberIndicesInPrimitives = metaData.getAlternateIdMemberIndicesInPrimitives();
        var compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

        if (compositeIndex.length == 1) {
            return primitives[compositeIndex[0]];
        }
        var compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
        var ids = new Object[compositeIndex.length];
        for (int a = compositeIndex.length; a-- > 0; ) {
            ids[a] = primitives[compositeIndex[a]];
        }
        return createCompositeId(metaData, compositeIdMember, ids);
    }

    @Override
    public Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue) {
        int[][] alternateIdMemberIndicesInPrimitives = metaData.getAlternateIdMemberIndicesInPrimitives();
        int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

        if (compositeIndex.length == 1) {
            return cacheValue.getPrimitive(compositeIndex[0]);
        }
        PrimitiveMember compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
        Object[] ids = new Object[compositeIndex.length];
        for (int a = compositeIndex.length; a-- > 0; ) {
            ids[a] = cacheValue.getPrimitive(compositeIndex[a]);
        }
        return createCompositeId(metaData, compositeIdMember, ids);
    }

    @Override
    public Object createIdFromEntity(IEntityMetaData metaData, int idIndex, Object entity) {
        int[][] alternateIdMemberIndicesInPrimitives = metaData.getAlternateIdMemberIndicesInPrimitives();
        int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

        if (compositeIndex.length == 1) {
            return metaData.getPrimitiveMembers()[compositeIndex[0]].getValue(entity);
        }
        PrimitiveMember compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
        PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
        Object[] ids = new Object[compositeIndex.length];
        for (int a = compositeIndex.length; a-- > 0; ) {
            ids[a] = primitiveMembers[compositeIndex[a]].getValue(entity);
        }
        return createCompositeId(metaData, compositeIdMember, ids);
    }
}
