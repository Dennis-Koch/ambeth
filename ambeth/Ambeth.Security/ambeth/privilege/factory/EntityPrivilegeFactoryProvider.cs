using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Privilege.Model.Impl;
using System;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public class EntityPrivilegeFactoryProvider : IEntityPrivilegeFactoryProvider
    {
        protected static readonly IEntityPrivilegeFactory ci = new DefaultEntityPrivilegeFactory();

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired(Optional = true)]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired]
        public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        protected readonly HashMap<Type, IEntityPrivilegeFactory[]> typeToConstructorMap = new HashMap<Type, IEntityPrivilegeFactory[]>();

        protected readonly Object writeLock = new Object();

        public IEntityPrivilegeFactory GetEntityPrivilegeFactory(Type entityType, bool create, bool read, bool update, bool delete, bool execute)
        {
            if (BytecodeEnhancer == null)
            {
                return ci;
            }
            int index = AbstractPrivilege.CalcIndex(create, read, update, delete, execute);
            IEntityPrivilegeFactory[] factories = typeToConstructorMap.Get(entityType);
            IEntityPrivilegeFactory factory = factories != null ? factories[index] : null;
            if (factory != null)
            {
                return factory;
            }
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                // concurrent thread might have been faster
                factories = typeToConstructorMap.Get(entityType);
                factory = factories != null ? factories[index] : null;
                if (factory != null)
                {
                    return factory;
                }
                try
                {
                    Type enhancedType = BytecodeEnhancer.GetEnhancedType(typeof(AbstractPrivilege), new EntityPrivilegeEnhancementHint(entityType, create, read,
                            update, delete, execute));

                    if (enhancedType == typeof(AbstractPrivilege))
                    {
                        // Nothing has been enhanced
                        factory = ci;
                    }
                    else
                    {
                        factory = AccessorTypeProvider.GetConstructorType<IEntityPrivilegeFactory>(enhancedType);
                    }
                }
                catch (Exception e)
                {
                    if (Log.WarnEnabled)
                    {
                        Log.Warn(e);
                    }
                    // something serious happened during enhancement: continue with a fallback
                    factory = ci;
                }
                if (factories == null)
                {
                    factories = new IEntityPrivilegeFactory[AbstractPrivilege.ArraySizeForIndex()];
                    typeToConstructorMap.Put(entityType, factories);
                }
                factories[index] = factory;
                return factory;
            }
        }
    }
}