package com.koch.ambeth.merge.bytecode.abstractobject;

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

import com.koch.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactory;
import com.koch.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactoryExtendable;
import com.koch.ambeth.bytecode.abstractobject.ImplementAbstractObjectEnhancementHint;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.extendable.IMapExtendableContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.ioc.proxy.Self;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IEntityInstantiationExtension;
import com.koch.ambeth.merge.IEntityInstantiationExtensionExtendable;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

import java.lang.reflect.ParameterizedType;

/**
 * ImplementAbstractObjectFactory implements objects based on interfaces. Optionally the
 * implementations can inherit from an (abstract) base type
 */
public class ImplementAbstractObjectFactory implements IDisposableBean, IImplementAbstractObjectFactory, IImplementAbstractObjectFactoryExtendable, IEntityInstantiationExtension, IInitializingBean {
    protected final IMapExtendableContainer<Class<?>, Class<?>> baseTypes = new MapExtendableContainer<>("baseType", "keyType");
    protected final IMapExtendableContainer<Class<?>, Class<?>[]> interfaceTypes = new MapExtendableContainer<>("interfaceTypes", "keyType");
    @Autowired
    protected IBytecodeEnhancer bytecodeEnhancer;
    @Autowired
    protected IEntityFactory entityFactory;
    @Autowired
    protected IEntityInstantiationExtensionExtendable entityInstantiationExtensionExtendable;
    @Autowired
    protected IPropertyInfoProvider propertyInfoProvider;
    @Self
    protected IEntityInstantiationExtension self;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        /**
         * TODO post processing of proxies did not occur (CallingProxyPostProcessor not involved)
         *
         * @see CallingProxyPostProcessor
         */
        if (self == null) {
            self = this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() throws Throwable {
        for (var entry : interfaceTypes.getExtensions().entrySet()) {
            unregisterInterfaceTypes(entry.getValue(), entry.getKey());
        }
        for (var entry : baseTypes.getExtensions().entrySet()) {
            unregisterBaseType(entry.getValue(), entry.getKey());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(Class<?> keyType) {
        if (keyType.isInterface()) {
            registerBaseType(getDefaultBaseType(keyType), keyType);
        } else {
            registerBaseType(keyType, keyType);
        }
    }

    /**
     * Returns the Default base Type for this keyType
     *
     * @param keyType The type to be implemented
     * @return The (abstract) base type to be extended
     */
    protected Class<?> getDefaultBaseType(Class<?> keyType) {
        return Object.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerBaseType(Class<?> baseType, Class<?> keyType) {
        var oldBaseType = baseTypes.getExtension(keyType);
        if (oldBaseType == null) {
            baseTypes.register(baseType, keyType);
            entityInstantiationExtensionExtendable.registerEntityInstantiationExtension(self, keyType);
        } else {
            baseTypes.unregister(oldBaseType, keyType);
            baseTypes.register(baseType, keyType);
        }

        // register keyType as interface
        if (keyType.isInterface()) {
            registerInterfaceTypes(new Class<?>[] { keyType }, keyType);
        }

        // register all interfaces implemented by baseType
        for (var interfaceType : baseType.getGenericInterfaces()) {
            if (interfaceType instanceof ParameterizedType) {
                interfaceType = ((ParameterizedType) interfaceType).getRawType();
            }
            var interfaceClass = (Class<?>) interfaceType;
            if (interfaceClass.isAssignableFrom(keyType)) {
                // registered above
                continue;
            }
            registerInterfaceTypes(new Class<?>[] { interfaceClass }, keyType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerInterfaceTypes(Class<?>[] interfaceTypes, Class<?> keyType) {
        if (!isRegistered(keyType)) {
            register(keyType);
        }
        var oldInterfaceTypes = this.interfaceTypes.getExtension(keyType);
        if (oldInterfaceTypes == null) {
            this.interfaceTypes.register(interfaceTypes, keyType);
        } else {
            // add to existing list
            var newInterfaceTypes = new Class<?>[oldInterfaceTypes.length + interfaceTypes.length];
            int index = 0;
            for (var interfaceType : oldInterfaceTypes) {
                newInterfaceTypes[index++] = interfaceType;
            }
            for (var interfaceType : interfaceTypes) {
                newInterfaceTypes[index++] = interfaceType;
            }
            this.interfaceTypes.unregister(oldInterfaceTypes, keyType);
            this.interfaceTypes.register(newInterfaceTypes, keyType);
        }
    }

    @Override
    public void unregister(Class<?> keyType) {
        if (keyType.isInterface()) {
            unregisterInterfaceTypes(new Class<?>[] { keyType }, keyType);
            unregisterBaseType(keyType, getDefaultBaseType(keyType));
        } else {
            unregisterBaseType(keyType, keyType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterBaseType(Class<?> baseType, Class<?> keyType) {
        var interfaceTypes = this.interfaceTypes.getExtension(keyType);
        if (interfaceTypes != null) {
            this.interfaceTypes.unregister(interfaceTypes, keyType);
        }
        baseTypes.unregister(baseType, keyType);
        entityInstantiationExtensionExtendable.unregisterEntityInstantiationExtension(self, keyType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterInterfaceTypes(Class<?>[] interfaceTypes, Class<?> keyType) {
        var oldInterfaceTypes = this.interfaceTypes.getExtension(keyType);
        if (oldInterfaceTypes != null) {
            // remove from existing
            var newInterfaceTypes = new Class<?>[oldInterfaceTypes.length - interfaceTypes.length];
            var index = 0;
            for (var oldInterfaceType : oldInterfaceTypes) {
                var remove = false;
                for (var toBeRemoved : interfaceTypes) {
                    if (oldInterfaceType == toBeRemoved) {
                        // remove this one
                        remove = true;
                        break;
                    }
                }
                if (!remove) {
                    newInterfaceTypes[index++] = oldInterfaceType;
                }
            }
            this.interfaceTypes.unregister(oldInterfaceTypes, keyType);
            if (newInterfaceTypes.length > 0) {
                this.interfaceTypes.register(newInterfaceTypes, keyType);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getBaseType(Class<?> keyType) {
        var baseType = baseTypes.getExtension(keyType);
        if (baseType == null) {
            throw new IllegalArgumentException("Type " + keyType.getName() + " is not registered for this extension");
        }
        return baseType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getInterfaceTypes(Class<?> keyType) {
        var interfaceTypes = this.interfaceTypes.getExtension(keyType);
        if (interfaceTypes == null) {
            if (!isRegistered(keyType)) {
                throw new IllegalArgumentException("Type " + keyType.getName() + " is not registered for this extension");
            }
            interfaceTypes = new Class<?>[0];
        }
        return interfaceTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRegistered(Class<?> keyType) {
        return baseTypes.getExtension(keyType) != null;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<? extends T> getImplementingType(Class<T> keyType) {
        if (isRegistered(keyType)) {
            return (Class<? extends T>) bytecodeEnhancer.getEnhancedType(keyType, ImplementAbstractObjectEnhancementHint.ImplementAbstractObjectEnhancementHint);
        }
        throw new IllegalArgumentException(keyType.getName() + " is not a registered type");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Class<? extends T> getMappedEntityType(Class<T> type) {
        return getImplementingType(type);
    }
}
