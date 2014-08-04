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

namespace De.Osthus.Ambeth.Proxy
{
    public class EntityFactory : AbstractEntityFactory, IEntityFactoryExtensionExtendable
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

        protected readonly ClassExtendableContainer<IEntityFactoryExtension> entityFactoryExtensions = new ClassExtendableContainer<IEntityFactoryExtension>(
			"entityFactoryExtension", "entityType");

        protected readonly HashMap<Type, ConstructorInfo> typeToConstructorMap = new HashMap<Type, ConstructorInfo>(0.5f);
        
        protected readonly Dictionary<Type, Dictionary<MethodInfo, int?>> typeToMethodMap = new Dictionary<Type, Dictionary<MethodInfo, int?>>();

    	protected readonly HashMap<Type, ConstructorInfo> typeToEmbbeddedParamConstructorMap = new HashMap<Type, ConstructorInfo>(0.5f);

      	protected readonly WeakDictionary<ConstructorInfo, Object[]> constructorToBeanArgsMap = new WeakDictionary<ConstructorInfo, Object[]>();

        protected readonly WeakDictionary<Type, IEmbeddedTypeInfoItem[][]> typeToEmbeddedInfoItemsMap = new WeakDictionary<Type, IEmbeddedTypeInfoItem[][]>();

        protected readonly Lock readLock, writeLock;

        public EntityFactory()
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

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
		    Object[] beanArgs = DictionaryExtension.ValueOrDefault(constructorToBeanArgsMap, constructor);
            if (beanArgs != null)
            {
                return beanArgs;
            }
            writeLock.Lock();
            try
            {
		        beanArgs = DictionaryExtension.ValueOrDefault(constructorToBeanArgsMap, constructor);
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
			    constructorToBeanArgsMap[constructor] = beanArgs;
            }
            finally
            {
                writeLock.Unlock();
            }
		    return beanArgs;
	    }

	    protected ConstructorInfo GetEmbeddedParamConstructor(Type embeddedType, Type parentEntityType)
	    {
		    ConstructorInfo constructor = typeToEmbbeddedParamConstructorMap.Get(embeddedType);
            if (constructor != null)
            {
                return constructor;
            }
		    writeLock.Lock();
            try
            {
                constructor = typeToEmbbeddedParamConstructorMap.Get(embeddedType);
		        if (constructor != null)
		        {
                    return constructor;
                }
			    try
			    {
                    constructor = embeddedType.GetConstructor(new Type[] { parentEntityType });
			    }
			    catch (Exception e)
			    {
				    if (BytecodePrinter != null)
				    {
					    throw RuntimeExceptionUtil.Mask(e, BytecodePrinter.ToPrintableBytecode(embeddedType));
				    }
				    throw;
			    }
			    typeToEmbbeddedParamConstructorMap.Put(embeddedType, constructor);
    		    return constructor;
            }
            finally
            {
                writeLock.Unlock();
            }
	    }
        
        public override Object CreateEntity(Type entityType)
        {
            Type mappedEntityType = entityType;
		    IEntityFactoryExtension extension = entityFactoryExtensions.GetExtension(entityType);
		    if (extension != null && extension != this)
		    {
			    mappedEntityType = extension.GetMappedEntityType(entityType);
		    }
		    IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(mappedEntityType);
		    if (metaData.EnhancedType == null)
		    {
                ((EntityMetaData)metaData).EnhancedType = BytecodeEnhancer.GetEnhancedType(mappedEntityType, EntityEnhancementHint.Instance);
                EntityMetaDataRefresher.RefreshMembers(metaData);
		    }
		    Object entity = CreateEntityIntern(metaData);
            if (extension != null && !Object.ReferenceEquals(extension, this))
		    {
			    entity = extension.PostProcessMappedEntity(entityType, metaData, entity);
		    }
		    return entity;
        }

        public override Object CreateEntity(IEntityMetaData metaData)
        {
            Type entityType = metaData.EntityType;
		    IEntityFactoryExtension extension = entityFactoryExtensions.GetExtension(entityType);
		    if (metaData.EnhancedType == null)
		    {
			    Type mappedEntityType = entityType;
			    if (extension != null && extension != this)
			    {
				    mappedEntityType = extension.GetMappedEntityType(mappedEntityType);
			    }
			    ((EntityMetaData) metaData).EnhancedType = BytecodeEnhancer.GetEnhancedType(mappedEntityType, EntityEnhancementHint.Instance);
                EntityMetaDataRefresher.RefreshMembers(metaData);
		    }
		    Object entity = CreateEntityIntern(metaData);
		    if (extension != null && !Object.ReferenceEquals(extension, this))
		    {
			    entity = extension.PostProcessMappedEntity(entityType, metaData, entity);
		    }
		    return entity;
        }

        protected Object CreateEntityIntern(IEntityMetaData metaData)
	    {
		    try
		    {
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

        protected IEmbeddedTypeInfoItem[][] GetEmbeddedTypeInfoItems(IEntityMetaData metaData)
	    {
            IEmbeddedTypeInfoItem[][] embeddedTypeInfoItems = DictionaryExtension.ValueOrDefault(typeToEmbeddedInfoItemsMap, metaData.EntityType);
		    if (embeddedTypeInfoItems != null)
		    {
			    return embeddedTypeInfoItems;
		    }
		    List<IEmbeddedTypeInfoItem> embeddedPrimitives = new List<IEmbeddedTypeInfoItem>();
            List<IEmbeddedTypeInfoItem> embeddedRelations = new List<IEmbeddedTypeInfoItem>();
            ITypeInfoItem idMember = metaData.IdMember;
		    if (idMember is IEmbeddedTypeInfoItem)
		    {
			    embeddedPrimitives.Add((IEmbeddedTypeInfoItem) idMember);
		    }
		    else if (idMember is CompositeIdTypeInfoItem)
		    {
			    foreach (ITypeInfoItem itemMember in ((CompositeIdTypeInfoItem) idMember).Members)
			    {
				    if (itemMember is IEmbeddedTypeInfoItem)
				    {
					    embeddedPrimitives.Add((IEmbeddedTypeInfoItem) itemMember);
				    }
			    }
		    }
		    foreach (ITypeInfoItem primitiveMember in metaData.PrimitiveMembers)
		    {
			    if (primitiveMember is IEmbeddedTypeInfoItem)
			    {
				    embeddedPrimitives.Add((IEmbeddedTypeInfoItem) primitiveMember);
			    }
		    }
            foreach (ITypeInfoItem relationMember in metaData.RelationMembers)
            {
                if (relationMember is IEmbeddedTypeInfoItem)
                {
                    embeddedPrimitives.Add((IEmbeddedTypeInfoItem)relationMember);
                }
            }
            embeddedTypeInfoItems = new IEmbeddedTypeInfoItem[][] { embeddedPrimitives.ToArray(), embeddedRelations.ToArray() };
		    typeToEmbeddedInfoItemsMap.Add(metaData.EntityType, embeddedTypeInfoItems);
		    return embeddedTypeInfoItems;
	    }
        
	    protected virtual void PostProcessEntity(Object entity, IEntityMetaData metaData)
	    {
		    ICacheModification cacheModification = CacheModification;
		    IBytecodeEnhancer bytecodeEnhancer = this.BytecodeEnhancer;
		    bool oldCacheModActive = cacheModification.Active;
		    cacheModification.Active = true;
		    try
		    {
			    IEmbeddedTypeInfoItem[][] embeddedTypeInfoItems = GetEmbeddedTypeInfoItems(metaData);
			    if (embeddedTypeInfoItems[0].Length > 0)
			    {
				    StringBuilder currPath = new StringBuilder();
				    foreach (IEmbeddedTypeInfoItem embeddedTypeInfoItem in embeddedTypeInfoItems[0])
				    {
					    HandleEmbeddedTypeInfoItem(entity, embeddedTypeInfoItem, currPath, true);
                        currPath.Length = 0;
                    }
			    }
                if (embeddedTypeInfoItems[1].Length > 0)
                {
                    StringBuilder currPath = new StringBuilder();
                    foreach (IEmbeddedTypeInfoItem embeddedTypeInfoItem in embeddedTypeInfoItems[1])
                    {
                        HandleEmbeddedTypeInfoItem(entity, embeddedTypeInfoItem, currPath, false);
                        currPath.Length = 0;
                    }
                }
                foreach (ITypeInfoItem primitiveMember in metaData.PrimitiveMembers)
			    {
				    // Check for embedded members
				    if (!(primitiveMember is IEmbeddedTypeInfoItem))
				    {
                        HandlePrimitiveMember(primitiveMember, entity);
					    continue;
				    }
			    }
		    }
		    finally
		    {
			    cacheModification.Active = oldCacheModActive;
		    }
	    }

        protected void HandleEmbeddedTypeInfoItem(Object entity, IEmbeddedTypeInfoItem member, StringBuilder currPath, bool isPrimitive)
	    {
			IBytecodeEnhancer bytecodeEnhancer = this.BytecodeEnhancer;
			ITypeInfoItem[] memberPath = member.MemberPath;
            Type entityType = entity.GetType();
			Object parentObject = entity;
            Object[] constructorArgs = new Object[1];
			foreach (ITypeInfoItem pathItem in memberPath)
			{
				Object embeddedObject = pathItem.GetValue(parentObject);
				if (embeddedObject != null)
				{
					parentObject = embeddedObject;
					currPath.Append(pathItem.Name).Append('.');
					continue;
				}
                currPath.Append(pathItem.Name);
                Type embeddedType = bytecodeEnhancer.GetEnhancedType(pathItem.RealType, new EmbeddedEnhancementHint(entityType, parentObject.GetType(), currPath.ToString()));
                ConstructorInfo embeddedConstructor = GetEmbeddedParamConstructor(embeddedType, parentObject.GetType());
                constructorArgs[0] = parentObject;
				embeddedObject = embeddedConstructor.Invoke(constructorArgs);
				pathItem.SetValue(parentObject, embeddedObject);
				parentObject = embeddedObject;
				currPath.Append('.');
			}
            if (isPrimitive)
            {
                HandlePrimitiveMember(member.ChildMember, parentObject);
            }
	    }

        protected void HandlePrimitiveMember(ITypeInfoItem primitiveMember, Object entity)
	    {
		    Type realType = primitiveMember.RealType;
		    if (typeof(IEnumerable).IsAssignableFrom(realType) && !typeof(String).Equals(realType))
		    {
			    Object primitive = primitiveMember.GetValue(entity);
			    if (primitive == null)
			    {
				    primitive = ListUtil.CreateObservableCollectionOfType(realType);
				    primitiveMember.SetValue(entity, primitive);
			    }
		    }
	    }

        public void RegisterEntityFactoryExtension(IEntityFactoryExtension entityFactoryExtension, Type type)
	    {
		    entityFactoryExtensions.Register(entityFactoryExtension, type);
	    }

	    public void UnregisterEntityFactoryExtension(IEntityFactoryExtension entityFactoryExtension, Type type)
	    {
		    entityFactoryExtensions.Unregister(entityFactoryExtension, type);
	    }
    }
}