package de.osthus.ambeth.proxy;

import java.util.Collection;

import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IBytecodePrinter;
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
import de.osthus.ambeth.util.ListUtil;

public class EntityFactory extends AbstractEntityFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired(optional = true)
	protected IBytecodePrinter bytecodePrinter;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IEntityMetaDataRefresher entityMetaDataRefresher;

	@Self
	protected IEntityFactory self;

	protected final SmartCopyMap<Class<?>, EntityFactoryConstructor> typeToConstructorMap = new SmartCopyMap<Class<?>, EntityFactoryConstructor>(0.5f);

	@Override
	public boolean supportsEnhancement(Class<?> enhancementType)
	{
		return bytecodeEnhancer.supportsEnhancement(enhancementType);
	}

	protected EntityFactoryConstructor getConstructorEntry(Class<?> entityType)
	{
		EntityFactoryConstructor constructor = typeToConstructorMap.get(entityType);
		if (constructor == null)
		{
			try
			{
				final EntityFactoryWithArgumentConstructor argumentConstructor = accessorTypeProvider.getConstructorType(
						EntityFactoryWithArgumentConstructor.class, entityType);
				constructor = new EntityFactoryConstructor()
				{
					@Override
					public Object createEntity()
					{
						return argumentConstructor.createEntity(self);
					}
				};
			}
			catch (Throwable e)
			{
				constructor = accessorTypeProvider.getConstructorType(EntityFactoryConstructor.class, entityType);
			}
			typeToConstructorMap.put(entityType, constructor);
		}
		return constructor;
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
			EntityFactoryConstructor constructor = getConstructorEntry(metaData.getEnhancedType());
			Object entity = constructor.createEntity();
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
