package de.osthus.ambeth.bytecode.abstractobject;

import java.lang.reflect.ParameterizedType;
import java.util.Map.Entry;

import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.IMapExtendableContainer;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.ioc.proxy.Self;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityFactoryExtension;
import de.osthus.ambeth.merge.IEntityFactoryExtensionExtendable;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

/**
 * ImplementAbstractObjectFactory implements objects based on interfaces. Optionally the implementations can inherit from an (abstract) base type
 */
public class ImplementAbstractObjectFactory implements IDisposableBean, IImplementAbstractObjectFactory, IImplementAbstractObjectFactoryExtendable,
		IEntityFactoryExtension, IInitializingBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityFactoryExtensionExtendable entityFactoryExtensionExtendable;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	protected final IMapExtendableContainer<Class<?>, Class<?>> baseTypes = new MapExtendableContainer<Class<?>, Class<?>>("baseType", "keyType");

	protected final IMapExtendableContainer<Class<?>, Class<?>[]> interfaceTypes = new MapExtendableContainer<Class<?>, Class<?>[]>("interfaceTypes", "keyType");

	@Self
	protected IEntityFactoryExtension self;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		/**
		 * TODO post processing of proxies did not occur (CallingProxyPostProcessor not involved)
		 * 
		 * @see CallingProxyPostProcessor
		 */
		if (self == null)
		{
			self = this;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() throws Throwable
	{
		for (Entry<Class<?>, Class<?>[]> entry : interfaceTypes.getExtensions().entrySet())
		{
			unregisterInterfaceTypes(entry.getValue(), entry.getKey());
		}
		for (Entry<Class<?>, Class<?>> entry : baseTypes.getExtensions().entrySet())
		{
			unregisterBaseType(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(Class<?> keyType)
	{
		if (keyType.isInterface())
		{
			registerBaseType(getDefaultBaseType(keyType), keyType);
		}
		else
		{
			registerBaseType(keyType, keyType);
		}
	}

	/**
	 * Returns the Default base Type for this keyType
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return The (abstract) base type to be extended
	 */
	protected Class<?> getDefaultBaseType(Class<?> keyType)
	{
		return Object.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerBaseType(Class<?> baseType, Class<?> keyType)
	{
		Class<?> oldBaseType = baseTypes.getExtension(keyType);
		if (oldBaseType == null)
		{
			baseTypes.register(baseType, keyType);
			entityFactoryExtensionExtendable.registerEntityFactoryExtension(self, keyType);
		}
		else
		{
			baseTypes.unregister(oldBaseType, keyType);
			baseTypes.register(baseType, keyType);
		}

		// register keyType as interface
		if (keyType.isInterface())
		{
			registerInterfaceTypes(new Class<?>[] { keyType }, keyType);
		}

		// register all interfaces implemented by baseType
		for (java.lang.reflect.Type interfaceType : baseType.getGenericInterfaces())
		{
			if (interfaceType instanceof ParameterizedType)
			{
				interfaceType = ((ParameterizedType) interfaceType).getRawType();
			}
			Class<?> interfaceClass = (Class<?>) interfaceType;
			if (interfaceClass.isAssignableFrom(keyType))
			{
				// registered above
				continue;
			}
			registerInterfaceTypes(new Class<?>[] { interfaceClass }, keyType);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerInterfaceTypes(Class<?>[] interfaceTypes, Class<?> keyType)
	{
		if (!isRegistered(keyType))
		{
			register(keyType);
		}
		Class<?>[] oldInterfaceTypes = this.interfaceTypes.getExtension(keyType);
		if (oldInterfaceTypes == null)
		{
			this.interfaceTypes.register(interfaceTypes, keyType);
		}
		else
		{
			// add to existing list
			Class<?>[] newInterfaceTypes = new Class<?>[oldInterfaceTypes.length + interfaceTypes.length];
			int index = 0;
			for (Class<?> interfaceType : oldInterfaceTypes)
			{
				newInterfaceTypes[index++] = interfaceType;
			}
			for (Class<?> interfaceType : interfaceTypes)
			{
				newInterfaceTypes[index++] = interfaceType;
			}
			this.interfaceTypes.unregister(oldInterfaceTypes, keyType);
			this.interfaceTypes.register(newInterfaceTypes, keyType);
		}
	}

	@Override
	public void unregister(Class<?> keyType)
	{
		if (keyType.isInterface())
		{
			unregisterInterfaceTypes(new Class<?>[] { keyType }, keyType);
			unregisterBaseType(keyType, getDefaultBaseType(keyType));
		}
		else
		{
			unregisterBaseType(keyType, keyType);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterBaseType(Class<?> baseType, Class<?> keyType)
	{
		Class<?>[] interfaceTypes = this.interfaceTypes.getExtension(keyType);
		if (interfaceTypes != null)
		{
			this.interfaceTypes.unregister(interfaceTypes, keyType);
		}
		baseTypes.unregister(baseType, keyType);
		entityFactoryExtensionExtendable.unregisterEntityFactoryExtension(self, keyType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterInterfaceTypes(Class<?>[] interfaceTypes, Class<?> keyType)
	{
		Class<?>[] oldInterfaceTypes = this.interfaceTypes.getExtension(keyType);
		if (oldInterfaceTypes != null)
		{
			// remove from existing
			Class<?>[] newInterfaceTypes = new Class<?>[oldInterfaceTypes.length - interfaceTypes.length];
			int index = 0;
			for (Class<?> oldInterfaceType : oldInterfaceTypes)
			{
				boolean remove = false;
				for (Class<?> toBeRemoved : interfaceTypes)
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
			this.interfaceTypes.unregister(oldInterfaceTypes, keyType);
			if (newInterfaceTypes.length > 0)
			{
				this.interfaceTypes.register(newInterfaceTypes, keyType);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getBaseType(Class<?> keyType)
	{
		Class<?> baseType = baseTypes.getExtension(keyType);
		if (baseType == null)
		{
			throw new IllegalArgumentException("Type " + keyType.getName() + " is not registered for this extension");
		}
		return baseType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getInterfaceTypes(Class<?> keyType)
	{
		Class<?>[] interfaceTypes = this.interfaceTypes.getExtension(keyType);
		if (interfaceTypes == null)
		{
			if (!isRegistered(keyType))
			{
				throw new IllegalArgumentException("Type " + keyType.getName() + " is not registered for this extension");
			}
			interfaceTypes = new Class<?>[0];
		}
		return interfaceTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRegistered(Class<?> keyType)
	{
		return baseTypes.getExtension(keyType) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> Class<? extends T> getImplementingType(Class<T> keyType)
	{
		if (isRegistered(keyType))
		{
			return (Class<? extends T>) bytecodeEnhancer.getEnhancedType(keyType, ImplementAbstractObjectContext.INSTANCE);
		}
		throw new IllegalArgumentException(keyType.getName() + " is not a registered type");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Class<? extends T> getMappedEntityType(Class<T> type)
	{
		return getImplementingType(type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object postProcessMappedEntity(Class<?> originalType, IEntityMetaData metaData, Object mappedEntity)
	{
		return mappedEntity;
	}
}