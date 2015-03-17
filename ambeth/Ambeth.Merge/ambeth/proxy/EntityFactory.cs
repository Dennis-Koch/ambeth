using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using System.Text;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Proxy;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Accessor;

namespace De.Osthus.Ambeth.Proxy
{
    public class EntityFactory : AbstractEntityFactory
    {
        [LogInstance]
        public ILogger log;

        [Autowired]
        public IAccessorTypeProvider AccessorTypeProvider { protected get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        [Autowired]
        public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }
        
        [Autowired(Optional = true)]
        public IBytecodePrinter BytecodePrinter { protected get; set; }

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public IEntityMetaDataRefresher EntityMetaDataRefresher { protected get; set; }
        
        [Autowired]
        public IProxyFactory ProxyFactory { protected get; set; }

        [Self]
        public IEntityFactory Self { protected get; set; }

        protected readonly SmartCopyMap<Type, EntityFactoryConstructor> typeToConstructorMap = new SmartCopyMap<Type, EntityFactoryConstructor>(0.5f);
        
        public override bool SupportsEnhancement(Type enhancementType)
	    {
		    return BytecodeEnhancer.SupportsEnhancement(enhancementType);
	    }

        protected EntityFactoryConstructor GetConstructorEntry(Type entityType)
	    {
            EntityFactoryConstructor constructor = typeToConstructorMap.Get(entityType);
            if (constructor == null)
		    {
                try
                {
                    EntityFactoryWithArgumentConstructor argumentConstructor = AccessorTypeProvider.GetConstructorType<EntityFactoryWithArgumentConstructor>(entityType);
                    constructor = new EntityFactoryToArgumentConstructor(argumentConstructor, Self);
                }
                catch (Exception)
                {
                    constructor = AccessorTypeProvider.GetConstructorType<EntityFactoryConstructor>(entityType);
                }
                typeToConstructorMap.Put(entityType, constructor);
		    }
            return constructor;
	    }

	    protected Object[] GetConstructorArguments(ConstructorInfo constructor)
	    {
		    ParameterInfo[] parameterTypes = constructor.GetParameters();
			Object[] beanArgs = new Object[parameterTypes.Length];
			for (int a = parameterTypes.Length; a-- > 0;)
			{
				Type parameterType = parameterTypes[a].ParameterType;
				if (typeof(IEntityFactory).Equals(parameterType))
				{
					beanArgs[a] = Self;
				}
				else
				{
					beanArgs[a] = BeanContext.GetService(parameterType);
				}
			}
		    return beanArgs;
	    }
        
        public override Object CreateEntity(Type entityType)
        {
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);
            return CreateEntityIntern(metaData, true);
        }

        public override Object CreateEntity(IEntityMetaData metaData)
        {
            return CreateEntityIntern(metaData, true);
        }

        protected Object CreateEntityIntern(IEntityMetaData metaData, bool doEmptyInit)
	    {
		    try
		    {
                if (metaData.EnhancedType == null)
                {
                    EntityMetaDataRefresher.RefreshMembers(metaData);
                }
                EntityFactoryConstructor constructor = GetConstructorEntry(metaData.EnhancedType);
                Object entity = constructor.CreateEntity();
				PostProcessEntity(entity, metaData);
				return entity;
		    }
		    catch (Exception e)
		    {
			    if (BytecodePrinter != null)
			    {
                    throw RuntimeExceptionUtil.Mask(e, BytecodePrinter.ToPrintableBytecode(metaData.EnhancedType));
			    }
			    throw;
		    }
	    }
        
	    protected virtual void PostProcessEntity(Object entity, IEntityMetaData metaData)
	    {
            if (entity is IBeanContextAware)
            {
                ((IBeanContextAware)entity).BeanContext = BeanContext;
            }
            metaData.PostProcessNewEntity(entity);
	    }

        protected void HandlePrimitiveMember(Member primitiveMember, Object entity)
	    {
		    Type realType = primitiveMember.RealType;
		    if (ListUtil.IsCollection(realType))
		    {
			    Object primitive = primitiveMember.GetValue(entity);
			    if (primitive == null)
			    {
				    primitive = ListUtil.CreateObservableCollectionOfType(realType);
				    primitiveMember.SetValue(entity, primitive);
			    }
		    }
	    }
    }
}