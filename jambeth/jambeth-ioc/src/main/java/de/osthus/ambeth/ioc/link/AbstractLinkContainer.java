package de.osthus.ambeth.ioc.link;

import java.util.Arrays;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.config.IDeclarationStackTraceAware;
import de.osthus.ambeth.ioc.exception.LinkException;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.util.IDelegateFactory;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractLinkContainer implements ILinkContainer, IInitializingBean, IDeclarationStackTraceAware
{
	public static final String PROPERTY_ARGUMENTS = "Arguments";

	public static final String PROPERTY_OPTIONAL = "Optional";

	public static final String PROPERTY_REGISTRY = "Registry";

	public static final String PROPERTY_REGISTRY_NAME = "RegistryBeanName";

	public static final String PROPERTY_REGISTRY_PROPERTY_NAME = "RegistryPropertyName";

	public static final String PROPERTY_LISTENER = "Listener";

	public static final String PROPERTY_LISTENER_BEAN = "ListenerBean";

	public static final String PROPERTY_LISTENER_NAME = "ListenerBeanName";

	public static final String PROPERTY_LISTENER_METHOD_NAME = "ListenerMethodName";

	public static final String PROPERTY_REGISTRY_TYPE = "RegistryBeanAutowiredType";

	protected static final Object[] emptyArgs = new Object[0];

	@Property(mandatory = false)
	protected Object listener;

	@Property(mandatory = false)
	protected IBeanConfiguration listenerBean;

	@Property(mandatory = false)
	protected String listenerBeanName;

	@Property(mandatory = false)
	protected String listenerMethodName;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IDelegateFactory delegateFactory;

	@Property(mandatory = false)
	protected Object registry;

	@Property(mandatory = false)
	protected Class<?> registryBeanAutowiredType;

	@Property(mandatory = false)
	protected String registryBeanName;

	@Property(mandatory = false)
	protected String registryPropertyName;

	@Property(mandatory = false)
	protected Object[] arguments;

	@Property(mandatory = false)
	protected boolean optional;

	protected Object resolvedListener;

	protected StackTraceElement[] declarationStackTrace;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertTrue(registryBeanAutowiredType != null || registryBeanName != null || registry != null,
				"either property 'RegistryBeanAutowiredType', 'RegistryBeanName' or 'Registry' must be valid");
		ParamChecker.assertTrue(listener != null || listenerBean != null || listenerBeanName != null,
				"either property 'Listener' or 'ListenerBean' or 'ListenerBeanName' must be valid");
		if (arguments == null)
		{
			arguments = emptyArgs;
		}
	}

	@Override
	public void setDeclarationStackTrace(StackTraceElement[] declarationStackTrace)
	{
		this.declarationStackTrace = declarationStackTrace;
	}

	protected Object resolveRegistry()
	{
		Object registry = this.registry;
		if (registry != null)
		{
			registry = resolveRegistryIntern(registry);
			this.registry = registry;
			return registry;
		}
		if (registryBeanName != null)
		{
			registry = beanContext.getService(registryBeanName, !optional);
		}
		else
		{
			registry = beanContext.getService(registryBeanAutowiredType, !optional);
		}
		registry = resolveRegistryIntern(registry);
		this.registry = registry;
		return registry;
	}

	protected Object resolveRegistryIntern(Object registry)
	{
		return registry;
	}

	protected Object resolveListener()
	{
		Object listener = this.listener;
		if (listener != null)
		{
			listener = resolveListenerIntern(listener);
			this.listener = listener;
			return listener;
		}
		else if (listenerBeanName != null)
		{
			listener = beanContext.getService(listenerBeanName, !optional);
			if (listener == null)
			{
				return null;
			}
		}
		else if (listenerBean != null)
		{
			listenerBeanName = listenerBean.getName();
			if (listenerBeanName == null)
			{
				listener = listenerBean.getInstance();
				if (listener == null)
				{
					throw new LinkException("No listener instance received from " + listenerBean.getClass().getName() + ".getInstance()"
							+ " to link to registry", this);
				}
			}
			else
			{
				listener = beanContext.getService(listenerBeanName, !optional);
				if (listener == null)
				{
					return null;
				}
			}
		}
		else
		{
			throw new LinkException("Listener not found. Must never happen.", this);
		}
		listener = resolveListenerIntern(listener);
		this.listener = listener;
		return listener;
	}

	protected Object resolveListenerIntern(Object listener)
	{
		if (listener == null)
		{
			throw new LinkException("Must never happen", this);
		}
		return listener;
	}

	@Override
	public void link()
	{
		Object registry = null, listener = null;
		try
		{
			registry = resolveRegistry();
			if (registry == null)
			{
				return;
			}
			listener = resolveListener();
			if (listener == null)
			{
				return;
			}
			handleLink(registry, listener);
		}
		catch (Exception e)
		{
			if (declarationStackTrace != null)
			{
				throw new LinkException("An error occured while trying to link " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener)
						+ " to " + (registryBeanName != null ? "'" + registryBeanName + "'" : registry) + "\n" + Arrays.toString(declarationStackTrace), e,
						this);
			}
			throw new LinkException("An error occured while trying to link " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener) + " to "
					+ (registryBeanName != null ? "'" + registryBeanName + "'" : registry), e, this);
		}
	}

	@Override
	public void unlink()
	{
		try
		{
			if (registry == null || listener == null)
			{
				// Nothing to do because there was no (successful) call to link() before
				ILogger log = getLog();
				if (!optional && log.isDebugEnabled())
				{
					log.debug("Unlink has been called without prior linking. If no other exception is visible in the logs then this may be a bug");
				}
				return;
			}
			handleUnlink(registry, listener);
		}
		catch (Exception e)
		{
			throw new LinkException("An error occured while trying to unlink " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener)
					+ " from " + (registryBeanName != null ? "'" + registryBeanName + "'" : registry), e, this);
		}
		finally
		{
			registry = null;
			listener = null;
		}
	}

	protected abstract ILogger getLog();

	protected abstract void handleLink(Object registry, Object listener) throws Exception;

	protected abstract void handleUnlink(Object registry, Object listener) throws Exception;
}
