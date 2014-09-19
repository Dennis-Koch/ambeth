package de.osthus.ambeth.proxy;

import java.util.Collection;
import java.util.Map;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IBytecodePrinter;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.collections.IdentityWeakSmartCopyMap;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.proxy.Self;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataRefresher;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.ListUtil;

public class EntityFactory extends AbstractEntityFactory
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
	protected IEntityMetaDataRefresher entityMetaDataRefresher;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Self
	protected IEntityFactory self;

	protected final SmartCopyMap<Class<?>, FastConstructor> typeToConstructorMap = new SmartCopyMap<Class<?>, FastConstructor>(0.5f);

	protected final IdentityWeakSmartCopyMap<FastConstructor, Object[]> constructorToBeanArgsMap = new IdentityWeakSmartCopyMap<FastConstructor, Object[]>(0.5f);

	@Override
	public boolean supportsEnhancement(Class<?> enhancementType)
	{
		return bytecodeEnhancer.supportsEnhancement(enhancementType);
	}

	protected <T> FastConstructor getConstructor(Map<Class<?>, FastConstructor> map, Class<T> entityType)
	{
		FastConstructor constructor = map.get(entityType);
		if (constructor == null)
		{
			FastClass fastClass = FastClass.create(entityType);
			Throwable lastThrowable = null;
			for (int a = 0, size = CONSTRUCTOR_SERIES.length; a < size; a++)
			{
				Class<?>[] parameters = CONSTRUCTOR_SERIES[a];
				try
				{
					constructor = fastClass.getConstructor(parameters);
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
			map.put(entityType, constructor);
		}
		return constructor;
	}

	protected Object[] getConstructorArguments(FastConstructor constructor)
	{
		Object[] beanArgs = constructorToBeanArgsMap.get(constructor);
		if (beanArgs != null)
		{
			return beanArgs;
		}
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
		return beanArgs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createEntity(Class<T> entityType)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		return (T) createEntityIntern(metaData, true);
	}

	@Override
	public Object createEntity(IEntityMetaData metaData)
	{
		return createEntityIntern(metaData, true);
	}

	@Override
	public Object createEntityNoEmptyInit(IEntityMetaData metaData)
	{
		return createEntityIntern(metaData, false);
	}

	protected Object createEntityIntern(IEntityMetaData metaData, boolean doEmptyInit)
	{
		try
		{
			if (metaData.getEnhancedType() == null)
			{
				entityMetaDataRefresher.refreshMembers(metaData);
			}
			FastConstructor constructor = getConstructor(typeToConstructorMap, metaData.getEnhancedType());
			Object[] args = getConstructorArguments(constructor);
			Object entity = constructor.newInstance(args);
			postProcessEntity(entity, metaData, doEmptyInit);
			return entity;
		}
		catch (Throwable e)
		{
			if (bytecodePrinter != null)
			{
				throw RuntimeExceptionUtil.mask(e, bytecodePrinter.toPrintableBytecode(metaData.getEnhancedType()));
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void postProcessEntity(Object entity, IEntityMetaData metaData, boolean doEmptyInit)
	{
		metaData.postProcessNewEntity(entity);
	}

	protected void handlePrimitiveMember(Member primitiveMember, Object entity)
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
}
