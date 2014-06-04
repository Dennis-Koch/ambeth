package de.osthus.ambeth.proxy;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IBytecodePrinter;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.collections.WeakSmartCopyMap;
import de.osthus.ambeth.compositeid.CompositeIdTypeInfoItem;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.ioc.proxy.Self;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityFactoryExtension;
import de.osthus.ambeth.merge.IEntityFactoryExtensionExtendable;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.typeinfo.IEmbeddedTypeInfoItem;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.ListUtil;

public class EntityFactory extends AbstractEntityFactory implements IEntityFactoryExtensionExtendable
{
	private static final Class<?>[][] CONSTRUCTOR_SERIES = { { IEntityFactory.class }, {} };

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired(optional = true)
	protected IBytecodePrinter bytecodePrinter;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Self
	protected IEntityFactory self;

	protected final ClassExtendableContainer<IEntityFactoryExtension> entityFactoryExtensions = new ClassExtendableContainer<IEntityFactoryExtension>(
			"entityFactoryExtension", "entityType");

	protected final SmartCopyMap<Class<?>, Constructor<?>> typeToConstructorMap = new SmartCopyMap<Class<?>, Constructor<?>>(0.5f);

	protected final SmartCopyMap<Class<?>, Constructor<?>> typeToEmbbeddedConstructorMap = new SmartCopyMap<Class<?>, Constructor<?>>(0.5f);

	protected final SmartCopyMap<Class<?>, Constructor<?>> typeToEmbbeddedParamConstructorMap = new SmartCopyMap<Class<?>, Constructor<?>>(0.5f);

	protected final HashMap<Class<?>, HashMap<Method, Integer>> typeToMethodMap = new HashMap<Class<?>, HashMap<Method, Integer>>();

	protected final WeakSmartCopyMap<Constructor<?>, Object[]> constructorToBeanArgsMap = new WeakSmartCopyMap<Constructor<?>, Object[]>(0.5f);

	protected final WeakSmartCopyMap<Class<?>, Reference<IEmbeddedTypeInfoItem[][]>> typeToEmbeddedInfoItemsMap = new WeakSmartCopyMap<Class<?>, Reference<IEmbeddedTypeInfoItem[][]>>(
			0.5f);

	protected final Lock readLock, writeLock;

	public EntityFactory()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
		typeToEmbeddedInfoItemsMap.setAutoCleanupReference(true);
	}

	@Override
	public boolean supportsEnhancement(Class<?> enhancementType)
	{
		return bytecodeEnhancer.supportsEnhancement(enhancementType);
	}

	@SuppressWarnings("unchecked")
	protected <T> Constructor<T> getConstructor(Map<Class<?>, Constructor<?>> map, Class<T> entityType)
	{
		Constructor<T> constructor = (Constructor<T>) map.get(entityType);
		if (constructor == null)
		{
			Throwable lastThrowable = null;
			for (int a = 0, size = CONSTRUCTOR_SERIES.length; a < size; a++)
			{
				Class<?>[] parameters = CONSTRUCTOR_SERIES[a];
				try
				{
					constructor = entityType.getDeclaredConstructor(parameters);
					lastThrowable = null;
					break;
				}
				catch (Throwable e)
				{
					lastThrowable = e;
				}
			}
			if (constructor == null)
			{
				throw RuntimeExceptionUtil.mask(lastThrowable);
			}
			constructor.setAccessible(true);
			map.put(entityType, constructor);
		}
		return constructor;
	}

	protected Object[] getConstructorArguments(Constructor<?> constructor)
	{
		Object[] beanArgs = constructorToBeanArgsMap.get(constructor);
		if (beanArgs == null)
		{
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			beanArgs = new Object[parameterTypes.length];
			for (int a = parameterTypes.length; a-- > 0;)
			{
				Class<?> parameterType = parameterTypes[a];
				if (IEntityFactory.class.equals(parameterType))
				{
					beanArgs[a] = self;
				}
				else
				{
					beanArgs[a] = beanContext.getService(parameterType);
				}
			}
			constructorToBeanArgsMap.put(constructor, beanArgs);
		}
		return beanArgs;
	}

	@SuppressWarnings("unchecked")
	protected <T> Constructor<T> getEmbeddedParamConstructor(Class<T> embeddedType, Class<?> parentObjectType)
	{
		Constructor<T> constructor = (Constructor<T>) typeToEmbbeddedParamConstructorMap.get(embeddedType);
		if (constructor == null)
		{
			try
			{
				constructor = embeddedType.getDeclaredConstructor(parentObjectType);
			}
			catch (Throwable e)
			{
				if (bytecodePrinter != null)
				{
					throw RuntimeExceptionUtil.mask(e, bytecodePrinter.toPrintableBytecode(embeddedType));
				}
				throw RuntimeExceptionUtil.mask(e);
			}
			constructor.setAccessible(true);
			typeToEmbbeddedParamConstructorMap.put(embeddedType, constructor);
		}
		return constructor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createEntity(Class<T> entityType)
	{
		IEntityFactoryExtension extension = entityFactoryExtensions.getExtension(entityType);
		if (extension != null && extension != this)
		{
			Class<? extends T> mappedEntityType = extension.getMappedEntityType(entityType);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(mappedEntityType);
			Object entity = createEntityIntern(metaData, mappedEntityType);
			return (T) extension.postProcessMappedEntity(entityType, metaData, entity);
		}
		return super.createEntity(entityType);
	}

	@Override
	public Object createEntity(IEntityMetaData metaData)
	{
		Class<?> entityType = metaData.getEntityType();
		IEntityFactoryExtension extension = entityFactoryExtensions.getExtension(entityType);
		Class<?> mappedEntityType = extension != null && extension != this ? extension.getMappedEntityType(entityType) : entityType;

		Object entity = createEntityIntern(metaData, mappedEntityType);
		if (extension != null && extension != this)
		{
			entity = extension.postProcessMappedEntity(entityType, metaData, entity);
		}
		return entity;
	}

	protected Object createEntityIntern(IEntityMetaData metaData, Class<?> entityType)
	{
		try
		{
			entityType = bytecodeEnhancer.getEnhancedType(entityType, EntityEnhancementHint.HOOK);
			if (!entityType.isInterface())
			{
				Constructor<?> constructor = getConstructor(typeToConstructorMap, entityType);
				Object[] args = getConstructorArguments(constructor);
				Object entity = constructor.newInstance(args);
				postProcessEntity(entity, metaData);
				return entity;
			}
			throw new IllegalArgumentException("It is not possible to create interface entities without bytecode enhancement");
		}
		catch (Throwable e)
		{
			if (bytecodePrinter != null)
			{
				throw RuntimeExceptionUtil.mask(e, bytecodePrinter.toPrintableBytecode(entityType));
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IEmbeddedTypeInfoItem[][] getEmbeddedTypeInfoItems(IEntityMetaData metaData)
	{
		IEmbeddedTypeInfoItem[][] embeddedTypeInfoItems = null;
		Reference<IEmbeddedTypeInfoItem[][]> embeddedTypeInfoItemsR = typeToEmbeddedInfoItemsMap.get(metaData.getEntityType());
		if (embeddedTypeInfoItemsR != null)
		{
			embeddedTypeInfoItems = embeddedTypeInfoItemsR.get();
		}
		if (embeddedTypeInfoItems != null)
		{
			return embeddedTypeInfoItems;
		}
		ArrayList<IEmbeddedTypeInfoItem> embeddedPrimitives = new ArrayList<IEmbeddedTypeInfoItem>();
		ArrayList<IEmbeddedTypeInfoItem> embeddedRelations = new ArrayList<IEmbeddedTypeInfoItem>();
		ITypeInfoItem idMember = metaData.getIdMember();
		if (idMember instanceof IEmbeddedTypeInfoItem)
		{
			embeddedPrimitives.add((IEmbeddedTypeInfoItem) idMember);
		}
		else if (idMember instanceof CompositeIdTypeInfoItem)
		{
			for (ITypeInfoItem itemMember : ((CompositeIdTypeInfoItem) idMember).getMembers())
			{
				if (itemMember instanceof IEmbeddedTypeInfoItem)
				{
					embeddedPrimitives.add((IEmbeddedTypeInfoItem) itemMember);
				}
			}
		}
		for (ITypeInfoItem primitiveMember : metaData.getPrimitiveMembers())
		{
			if (primitiveMember instanceof IEmbeddedTypeInfoItem)
			{
				embeddedPrimitives.add((IEmbeddedTypeInfoItem) primitiveMember);
			}
		}
		for (ITypeInfoItem relationMember : metaData.getRelationMembers())
		{
			if (relationMember instanceof IEmbeddedTypeInfoItem)
			{
				embeddedRelations.add((IEmbeddedTypeInfoItem) relationMember);
			}
		}
		embeddedTypeInfoItems = new IEmbeddedTypeInfoItem[][] { embeddedPrimitives.toArray(IEmbeddedTypeInfoItem.class),
				embeddedRelations.toArray(IEmbeddedTypeInfoItem.class) };
		typeToEmbeddedInfoItemsMap.put(metaData.getEntityType(), new SoftReference<IEmbeddedTypeInfoItem[][]>(embeddedTypeInfoItems));
		return embeddedTypeInfoItems;
	}

	protected void postProcessEntity(Object entity, IEntityMetaData metaData)
	{
		ICacheModification cacheModification = this.cacheModification;
		boolean oldCacheModActive = cacheModification.isActive();
		cacheModification.setActive(true);
		try
		{
			IEmbeddedTypeInfoItem[][] embeddedTypeInfoItems = getEmbeddedTypeInfoItems(metaData);
			if (embeddedTypeInfoItems[0].length > 0)
			{
				StringBuilder currPath = new StringBuilder();
				for (IEmbeddedTypeInfoItem embeddedTypeInfoItem : embeddedTypeInfoItems[0])
				{
					handleEmbeddedTypeInfoItem(entity, embeddedTypeInfoItem, currPath, true);
					currPath.setLength(0);
				}
			}
			if (embeddedTypeInfoItems[1].length > 0)
			{
				StringBuilder currPath = new StringBuilder();
				for (IEmbeddedTypeInfoItem embeddedTypeInfoItem : embeddedTypeInfoItems[1])
				{
					handleEmbeddedTypeInfoItem(entity, embeddedTypeInfoItem, currPath, false);
					currPath.setLength(0);
				}
			}
			for (ITypeInfoItem primitiveMember : metaData.getPrimitiveMembers())
			{
				// Check for embedded members
				if (!(primitiveMember instanceof IEmbeddedTypeInfoItem))
				{
					handlePrimitiveMember(primitiveMember, entity);
					continue;
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			cacheModification.setActive(oldCacheModActive);
		}
	}

	protected void handleEmbeddedTypeInfoItem(Object entity, IEmbeddedTypeInfoItem member, StringBuilder currPath, boolean isPrimitive)
	{
		try
		{
			IBytecodeEnhancer bytecodeEnhancer = this.bytecodeEnhancer;
			ITypeInfoItem[] memberPath = member.getMemberPath();
			Class<?> entityType = entity.getClass();
			Object[] constructorArgs = new Object[1];
			Object parentObject = entity;
			for (ITypeInfoItem pathItem : memberPath)
			{
				Object embeddedObject = pathItem.getValue(parentObject);
				if (embeddedObject != null)
				{
					parentObject = embeddedObject;
					currPath.append(pathItem.getName()).append('.');
					continue;
				}
				currPath.append(pathItem.getName());
				Class<?> embeddedType = bytecodeEnhancer.getEnhancedType(pathItem.getRealType(),
						new EmbeddedEnhancementHint(entityType, parentObject.getClass(), currPath.toString()));
				Constructor<?> embeddedConstructor = getEmbeddedParamConstructor(embeddedType, parentObject.getClass());
				constructorArgs[0] = parentObject;
				embeddedObject = embeddedConstructor.newInstance(constructorArgs);
				pathItem.setValue(parentObject, embeddedObject);

				parentObject = embeddedObject;
				currPath.append('.');
			}
			if (isPrimitive)
			{
				handlePrimitiveMember(member.getChildMember(), parentObject);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void handlePrimitiveMember(ITypeInfoItem primitiveMember, Object entity)
	{
		Class<?> realType = primitiveMember.getRealType();
		if (Collection.class.isAssignableFrom(realType))
		{
			Object primitive = primitiveMember.getValue(entity);
			if (primitive == null)
			{
				primitive = ListUtil.createObservableCollectionOfType(realType);
				primitiveMember.setValue(entity, primitive);
			}
		}
	}

	@Override
	public void registerEntityFactoryExtension(IEntityFactoryExtension entityFactoryExtension, Class<?> type)
	{
		entityFactoryExtensions.register(entityFactoryExtension, type);
	}

	@Override
	public void unregisterEntityFactoryExtension(IEntityFactoryExtension entityFactoryExtension, Class<?> type)
	{
		entityFactoryExtensions.unregister(entityFactoryExtension, type);
	}
}