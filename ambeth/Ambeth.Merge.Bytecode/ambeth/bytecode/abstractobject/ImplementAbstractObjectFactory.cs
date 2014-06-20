using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Proxy;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Bytecode.AbstractObject
{
    /**
     * ImplementAbstractObjectFactory implements objects based on interfaces. Optionally the implementations can inherit from an (abstract) base type
     */
    public class ImplementAbstractObjectFactory : IDisposableBean, IImplementAbstractObjectFactory, IImplementAbstractObjectFactoryExtendable,
            IEntityFactoryExtension, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IEntityFactoryExtensionExtendable EntityFactoryExtensionExtendable { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        protected readonly IMapExtendableContainer<Type, Type> baseTypes = new MapExtendableContainer<Type, Type>("baseType", "keyType");

        protected readonly IMapExtendableContainer<Type, Type[]> interfaceTypes = new MapExtendableContainer<Type, Type[]>("interfaceTypes", "keyType");

        [Self]
        public IEntityFactoryExtension Self { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            /**
             * TODO post processing of proxies did not occur (CallingProxyPostProcessor not involved)
             * 
             * @see CallingProxyPostProcessor
             */
            if (Self == null)
            {
                Self = this;
            }
        }

        /**
         * {@inheritDoc}
         */
        public void Destroy()
        {
            foreach (Entry<Type, Type[]> entry in interfaceTypes.GetExtensions())
            {
                UnregisterInterfaceTypes(entry.Value, entry.Key);
            }
            foreach (Entry<Type, Type> entry in baseTypes.GetExtensions())
            {
                UnregisterBaseType(entry.Value, entry.Key);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void Register(Type keyType)
        {
            if (keyType.IsInterface)
            {
                RegisterBaseType(GetDefaultBaseType(keyType), keyType);
            }
            else
            {
                RegisterBaseType(keyType, keyType);
            }
        }

        /**
         * Returns the Default base Type for this keyType
         * 
         * @param keyType
         *            The type to be implemented
         * @return The (abstract) base type to be extended
         */
        protected Type GetDefaultBaseType(Type keyType)
        {
            return typeof(Object);
        }

        /**
         * {@inheritDoc}
         */
        public void RegisterBaseType(Type baseType, Type keyType)
        {
            Type oldBaseType = baseTypes.GetExtension(keyType);
            if (oldBaseType == null)
            {
                baseTypes.Register(baseType, keyType);
                EntityFactoryExtensionExtendable.RegisterEntityFactoryExtension(Self, keyType);
            }
            else
            {
                baseTypes.Unregister(oldBaseType, keyType);
                baseTypes.Register(baseType, keyType);
            }

            // register keyType as interface
            if (keyType.IsInterface)
            {
                RegisterInterfaceTypes(new Type[] { keyType }, keyType);
            }

            // register all interfaces implemented by baseType
            foreach (Type interfaceClass in baseType.GetInterfaces())
            {
                if (interfaceClass.IsAssignableFrom(keyType))
                {
                    // registered above
                    continue;
                }
                RegisterInterfaceTypes(new Type[] { interfaceClass }, keyType);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void RegisterInterfaceTypes(Type[] interfaceTypes, Type keyType)
        {
            if (!IsRegistered(keyType))
            {
                Register(keyType);
            }
            Type[] oldInterfaceTypes = this.interfaceTypes.GetExtension(keyType);
            if (oldInterfaceTypes == null)
            {
                this.interfaceTypes.Register(interfaceTypes, keyType);
            }
            else
            {
                // add to existing list
                Type[] newInterfaceTypes = new Type[oldInterfaceTypes.Length + interfaceTypes.Length];
                int index = 0;
                foreach (Type interfaceType in oldInterfaceTypes)
                {
                    newInterfaceTypes[index++] = interfaceType;
                }
                foreach (Type interfaceType in interfaceTypes)
                {
                    newInterfaceTypes[index++] = interfaceType;
                }
                this.interfaceTypes.Unregister(oldInterfaceTypes, keyType);
                this.interfaceTypes.Register(newInterfaceTypes, keyType);
            }
        }

        public void Unregister(Type keyType)
        {
            if (keyType.IsInterface)
            {
                UnregisterInterfaceTypes(new Type[] { keyType }, keyType);
                UnregisterBaseType(keyType, GetDefaultBaseType(keyType));
            }
            else
            {
                UnregisterBaseType(keyType, keyType);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void UnregisterBaseType(Type baseType, Type keyType)
        {
            Type[] interfaceTypes = this.interfaceTypes.GetExtension(keyType);
            if (interfaceTypes != null)
            {
                this.interfaceTypes.Unregister(interfaceTypes, keyType);
            }
            baseTypes.Unregister(baseType, keyType);
            EntityFactoryExtensionExtendable.UnregisterEntityFactoryExtension(Self, keyType);
        }

        /**
         * {@inheritDoc}
         */
        public void UnregisterInterfaceTypes(Type[] interfaceTypes, Type keyType)
        {
            Type[] oldInterfaceTypes = this.interfaceTypes.GetExtension(keyType);
            if (oldInterfaceTypes != null)
            {
                // remove from existing
                Type[] newInterfaceTypes = new Type[oldInterfaceTypes.Length - interfaceTypes.Length];
                int index = 0;
                foreach (Type oldInterfaceType in oldInterfaceTypes)
                {
                    bool remove = false;
                    foreach (Type toBeRemoved in interfaceTypes)
                    {
                        if (oldInterfaceType == toBeRemoved)
                        {
                            // remove this one
                            remove = true;
                            break;
                        }
                    }
                    if (!remove)
                    {
                        newInterfaceTypes[index++] = oldInterfaceType;
                    }
                }
                this.interfaceTypes.Unregister(oldInterfaceTypes, keyType);
                if (newInterfaceTypes.Length > 0)
                {
                    this.interfaceTypes.Register(newInterfaceTypes, keyType);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public Type GetBaseType(Type keyType)
        {
            Type baseType = baseTypes.GetExtension(keyType);
            if (baseType == null)
            {
                throw new ArgumentException("Type " + keyType.FullName + " is not registered for this extension");
            }
            return baseType;
        }

        /**
         * {@inheritDoc}
         */
        public Type[] GetInterfaceTypes(Type keyType)
        {
            Type[] interfaceTypes = this.interfaceTypes.GetExtension(keyType);
            if (interfaceTypes == null)
            {
                if (!IsRegistered(keyType))
                {
                    throw new ArgumentException("Type " + keyType.FullName + " is not registered for this extension");
                }
                interfaceTypes = new Type[0];
            }
            return interfaceTypes;
        }

        /**
         * {@inheritDoc}
         */
        public bool IsRegistered(Type keyType)
        {
            return baseTypes.GetExtension(keyType) != null;
        }

        /**
         * {@inheritDoc}
         */
        public Type GetImplementingType(Type keyType)
        {
            if (IsRegistered(keyType))
            {
                return BytecodeEnhancer.GetEnhancedType(keyType, ImplementAbstractObjectEnhancementHint.INSTANCE);
            }
            throw new ArgumentException(keyType.FullName + " is not a registered type");
        }

        /**
         * {@inheritDoc}
         */
        public Type GetMappedEntityType(Type type)
        {
            return GetImplementingType(type);
        }

        /**
         * {@inheritDoc}
         */
        public virtual Object PostProcessMappedEntity(Type originalType, IEntityMetaData metaData, Object mappedEntity)
        {
            return mappedEntity;
        }
    }
}