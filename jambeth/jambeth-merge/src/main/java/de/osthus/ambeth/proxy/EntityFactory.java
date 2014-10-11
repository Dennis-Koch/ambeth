package de.osthus.ambeth.proxy;

import java.util.Collection;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IBytecodePrinter;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IBeanContextAware;
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
	public static class ConstructorEntry
	{
		public final FastConstructor constructor;

		public final Object[] args;

		public ConstructorEntry(FastConstructor constructor, Object[] args)
		{
			super();
			this.constructor = constructor;
			this.args = args;
		}
	}

	private static final Class<?>[][] CONSTRUCTOR_SERIES = { { IEntityFactory.class }, {} };

	private static final Object[] EMPTY_ARGS = new Object[0];

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

	protected final SmartCopyMap<Class<?>, ConstructorEntry> typeToConstructorMap = new SmartCopyMap<Class<?>, ConstructorEntry>(0.5f);

	@Override
	public boolean supportsEnhancement(Class<?> enhancementType)
	{
		return bytecodeEnhancer.supportsEnhancement(enhancementType);
	}

	protected ConstructorEntry getConstructorEntry(Class<?> entityType)
	{
		ConstructorEntry constructorEntry = typeToConstructorMap.get(entityType);
		if (constructorEntry == null)
		{
			FastClass fastClass = FastClass.create(entityType);
			Throwable lastThrowable = null;
			FastConstructor constructor = null;
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
			constructorEntry = new ConstructorEntry(constructor, getConstructorArguments(constructor));
			typeToConstructorMap.put(entityType, constructorEntry);
		}
		return constructorEntry;
	}

	protected Object[] getConstructorArguments(FastConstructor constructor)
	{
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		if (parameterTypes.length == 0)
		{
			return EMPTY_ARGS;
		}
		Object[] beanArgs = new Object[parameterTypes.length];
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
			ConstructorEntry constructorEntry = getConstructorEntry(metaData.getEnhancedType());
			Object entity = constructorEntry.constructor.newInstance(constructorEntry.args);
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
		if (entity instanceof IBeanContextAware)
		{
			((IBeanContextAware) entity).setBeanContext(beanContext);
		}
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
