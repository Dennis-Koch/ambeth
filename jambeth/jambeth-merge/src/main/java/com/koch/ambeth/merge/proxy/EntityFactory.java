package com.koch.ambeth.merge.proxy;

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

import com.koch.ambeth.ioc.IBeanContextAware;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.proxy.Self;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.bytecode.IBytecodePrinter;
import com.koch.ambeth.service.merge.IEntityMetaDataRefresher;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.util.Collection;
import java.util.List;

public class EntityFactory extends AbstractEntityFactory {
    protected final SmartCopyMap<Class<?>, EntityFactoryConstructor> typeToConstructorMap = new SmartCopyMap<>(0.5f);
    @Autowired
    protected IAccessorTypeProvider accessorTypeProvider;
    @Autowired
    protected IServiceContext beanContext;
    @Autowired(optional = true)
    protected IBytecodePrinter bytecodePrinter;
    @Autowired
    protected IBytecodeEnhancer bytecodeEnhancer;
    @Autowired
    protected IEntityMetaDataRefresher entityMetaDataRefresher;
    @Self
    protected IEntityFactory self;

    @Override
    public boolean supportsEnhancement(Class<?> enhancementType) {
        return bytecodeEnhancer.supportsEnhancement(enhancementType);
    }

    protected EntityFactoryConstructor getConstructorEntry(Class<?> entityType) {
        var constructor = typeToConstructorMap.get(entityType);
        if (constructor == null) {
            try {
                var argumentConstructor = accessorTypeProvider.getConstructorType(EntityFactoryWithArgumentConstructor.class, entityType);
                constructor = new EntityFactoryConstructor() {
                    @Override
                    public Object createEntity() {
                        return argumentConstructor.createEntity(self);
                    }
                };
            } catch (Throwable e) {
                constructor = accessorTypeProvider.getConstructorType(EntityFactoryConstructor.class, entityType);
            }
            typeToConstructorMap.put(entityType, constructor);
        }
        return constructor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createEntity(Class<T> entityType) {
        var metaData = entityMetaDataProvider.getMetaData(entityType);
        return createEntityIntern(metaData, true);
    }

    @Override
    public <T> List<T> createEntity(Class<T> entityType, int amount) {
        var metaData = entityMetaDataProvider.getMetaData(entityType);
        return createEntityIntern(metaData, true, amount);
    }

    @Override
    public <T> T createEntity(IEntityMetaData metaData) {
        return createEntityIntern(metaData, true);
    }

    @Override
    public <T> List<T> createEntity(IEntityMetaData metaData, int amount) {
        return createEntityIntern(metaData, true, amount);
    }

    @Override
    public <T> T createEntityNoEmptyInit(IEntityMetaData metaData) {
        return createEntityIntern(metaData, false);
    }

    @Override
    public <T> List<T> createEntityNoEmptyInit(IEntityMetaData metaData, int amount) {
        return createEntityIntern(metaData, false, amount);
    }

    protected <T> T createEntityIntern(IEntityMetaData metaData, boolean doEmptyInit) {
        try {
            if (metaData.getEnhancedType() == null) {
                entityMetaDataRefresher.refreshMembers(metaData);
            }
            var constructor = getConstructorEntry(metaData.getEnhancedType());
            var entity = constructor.createEntity();
            postProcessEntity(entity, metaData, doEmptyInit);
            return (T) entity;
        } catch (Throwable e) {
            if (bytecodePrinter != null) {
                throw RuntimeExceptionUtil.mask(e, bytecodePrinter.toPrintableBytecode(metaData.getEnhancedType()));
            }
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    protected <T> List<T> createEntityIntern(IEntityMetaData metaData, boolean doEmptyInit, int amount) {
        try {
            if (metaData.getEnhancedType() == null) {
                entityMetaDataRefresher.refreshMembers(metaData);
            }
            var constructor = getConstructorEntry(metaData.getEnhancedType());
            var list = new ArrayList<T>(amount);
            for (int a = amount; a-- > 0; ) {
                var entity = constructor.createEntity();
                postProcessEntity(entity, metaData, doEmptyInit);
                list.add((T) entity);
            }
            return list;
        } catch (Throwable e) {
            if (bytecodePrinter != null) {
                throw RuntimeExceptionUtil.mask(e, bytecodePrinter.toPrintableBytecode(metaData.getEnhancedType()));
            }
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    protected void postProcessEntity(Object entity, IEntityMetaData metaData, boolean doEmptyInit) {
        if (entity instanceof IBeanContextAware) {
            ((IBeanContextAware) entity).setBeanContext(beanContext);
        }
        metaData.postProcessNewEntity(entity);
    }

    protected void handlePrimitiveMember(Member primitiveMember, Object entity) {
        var realType = primitiveMember.getRealType();
        if (Collection.class.isAssignableFrom(realType)) {
            var primitive = primitiveMember.getValue(entity);
            if (primitive == null) {
                primitive = ListUtil.createObservableCollectionOfType(realType);
                primitiveMember.setValue(entity, primitive);
            }
        }
    }
}
