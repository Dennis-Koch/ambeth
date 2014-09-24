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

namespace De.Osthus.Ambeth.Proxy
{
    public class EntityFactory : AbstractEntityFactory
    {
        private static readonly Type[][] CONSTRUCTOR_SERIES = new Type[][] { new Type[] { typeof(IEntityFactory) }, new Type[] {} };

        [LogInstance]
        public ILogger log;

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

        protected readonly SmartCopyMap<Type, ConstructorInfo> typeToConstructorMap = new SmartCopyMap<Type, ConstructorInfo>(0.5f);
        
        protected readonly IdentityWeakSmartCopyMap<ConstructorInfo, Object[]> constructorToBeanArgsMap = new IdentityWeakSmartCopyMap<ConstructorInfo, Object[]>();

        public override bool SupportsEnhancement(Type enhancementType)
	    {
		    return BytecodeEnhancer.SupportsEnhancement(enhancementType);
	    }

	    protected ConstructorInfo GetConstructor(IMap<Type, ConstructorInfo> map, Type entityType)
	    {
		    ConstructorInfo constructor = map.Get(entityType);
		    if (constructor == null)
		    {
			    Exception lastThrowable = null;
			    for (int a = 0, size = CONSTRUCTOR_SERIES.Length; a < size; a++)
			    {
				    Type[] parameters = CONSTRUCTOR_SERIES[a];
				    try
				    {
					    constructor = entityType.GetConstructor(parameters);
                        if (constructor != null)
                        {
                            lastThrowable = null;
                            break;
                        }
				    }
				    catch (Exception e)
				    {
					    lastThrowable = e;
				    }
			    }
			    if (constructor == null)
			    {
				    throw lastThrowable;
			    }
			    map.Put(entityType, constructor);
		    }
		    return constructor;
	    }

	    protected Object[] GetConstructorArguments(ConstructorInfo constructor)
	    {
            Object[] beanArgs = constructorToBeanArgsMap.Get(constructor);
            if (beanArgs != null)
            {
                return beanArgs;
            }
		    ParameterInfo[] parameterTypes = constructor.GetParameters();
			beanArgs = new Object[parameterTypes.Length];
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
			constructorToBeanArgsMap.Put(constructor, beanArgs);
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
                ConstructorInfo constructor = GetConstructor(typeToConstructorMap, metaData.EnhancedType);
				Object[] args = GetConstructorArguments(constructor);
				Object entity = constructor.Invoke(args);
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