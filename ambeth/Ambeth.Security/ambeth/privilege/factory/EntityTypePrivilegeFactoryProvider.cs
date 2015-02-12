using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Privilege.Model.Impl;
using System;

namespace De.Osthus.Ambeth.Privilege.Factory
{
    public class EntityTypePrivilegeFactoryProvider : IEntityTypePrivilegeFactoryProvider
    {
        protected static readonly IEntityTypePrivilegeFactory ci = new DefaultEntityTypePrivilegeFactory();


        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired(Optional = true)]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

        [Autowired]
        public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        protected readonly HashMap<Type, IEntityTypePrivilegeFactory[]> typeToConstructorMap = new HashMap<Type, IEntityTypePrivilegeFactory[]>();

        protected readonly Object writeLock = new Object();

        public IEntityTypePrivilegeFactory GetEntityTypePrivilegeFactory(Type entityType, bool? create, bool? read, bool? update, bool? delete,
                bool? execute)
        {
            if (BytecodeEnhancer == null)
            {
                return ci;
            }
            int index = AbstractTypePrivilege.CalcIndex(create, read, update, delete, execute);
            IEntityTypePrivilegeFactory[] factories = typeToConstructorMap.Get(entityType);
            IEntityTypePrivilegeFactory factory = factories != null ? factories[index] : null;
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
                    Type enhancedType = BytecodeEnhancer.GetEnhancedType(typeof(AbstractTypePrivilege), new EntityTypePrivilegeEnhancementHint(entityType,
                            create, read, update, delete, execute));

                    if (enhancedType == typeof(AbstractTypePrivilege))
                    {
                        // Nothing has been enhanced
                        factory = ci;
                    }
                    else
                    {
                        factory = AccessorTypeProvider.GetConstructorType<IEntityTypePrivilegeFactory>(enhancedType);
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
                    factories = new IEntityTypePrivilegeFactory[AbstractTypePrivilege.ArraySizeForIndex()];
                    typeToConstructorMap.Put(entityType, factories);
                }
                factories[index] = factory;
                return factory;
            }
        }
    }
}