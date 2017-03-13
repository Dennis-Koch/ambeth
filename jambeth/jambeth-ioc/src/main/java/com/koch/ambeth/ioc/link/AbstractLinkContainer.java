package com.koch.ambeth.ioc.link;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.IDeclarationStackTraceAware;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.exception.LinkException;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.util.IDelegateFactory;
import com.koch.ambeth.util.ParamChecker;

public abstract class AbstractLinkContainer implements ILinkContainer, IInitializingBean, IDeclarationStackTraceAware
{
	public static class LinkDisposable extends WeakReference<AbstractLinkContainer> implements IDisposableBean
	{
		private final int linkCounter;

		public LinkDisposable(AbstractLinkContainer target)
		{
			super(target);
			linkCounter = target.linkCounter;
		}

		@Override
		public void destroy() throws Throwable
		{
			AbstractLinkContainer target = get();
			if (target == null || linkCounter != target.linkCounter || !target.linked)
			{
				// this delegate is already outdated
				return;
			}
			target.unlink();
		}
	}

	public static final String PROPERTY_ARGUMENTS = "Arguments";

	public static final String PROPERTY_OPTIONAL = "Optional";

	public static final String PROPERTY_REGISTRY = "Registry";

	public static final String PROPERTY_REGISTRY_PROPERTY_NAME = "RegistryPropertyName";

	public static final String PROPERTY_LISTENER = "Listener";

	public static final String PROPERTY_LISTENER_BEAN = "ListenerBean";

	public static final String PROPERTY_LISTENER_NAME = "ListenerBeanName";

	public static final String PROPERTY_LISTENER_METHOD_NAME = "ListenerMethodName";

	public static final String PROPERTY_REGISTRY_TYPE = "RegistryBeanAutowiredType";

	public static final String PROPERTY_FOREIGN_BEAN_CONTEXT = "ForeignBeanContext";

	public static final String PROPERTY_FOREIGN_BEAN_CONTEXT_NAME = "ForeignBeanContextName";

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

	@Property(mandatory = false)
	protected IServiceContext foreignBeanContext;

	@Property(mandatory = false)
	protected String foreignBeanContextName;

	@Autowired
	protected IDelegateFactory delegateFactory;

	@Property(mandatory = false)
	protected Object registry;

	@Property(mandatory = false)
	protected Class<?> registryBeanAutowiredType;

	@Property(mandatory = false)
	protected String registryPropertyName;

	@Property(mandatory = false)
	protected Object[] arguments;

	@Property(mandatory = false)
	protected boolean optional;

	protected Object resolvedListener;

	protected StackTraceElement[] declarationStackTrace;

	protected boolean linked;

	protected int linkCounter;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertTrue(registryBeanAutowiredType != null || registry != null,
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
		IServiceContext beanContext = this.beanContext;
		boolean hasForeignContextBeenUsed = true;
		if (foreignBeanContext != null)
		{
			beanContext = foreignBeanContext;
			hasForeignContextBeenUsed = false;
		}
		else if (foreignBeanContextName != null)
		{
			foreignBeanContext = beanContext.getService(foreignBeanContextName, IServiceContext.class, !optional);
			beanContext = foreignBeanContext;
			hasForeignContextBeenUsed = false;
		}
		if (beanContext == null)
		{
			return null;
		}
		Object registry = this.registry;
		if (registry instanceof Class)
		{
			registry = beanContext.getService((Class<?>) registry, !optional);
			hasForeignContextBeenUsed = true;
		}
		else if (registry instanceof String)
		{
			registry = beanContext.getService((String) registry, !optional);
			hasForeignContextBeenUsed = true;
		}
		else if (registry instanceof IBeanConfiguration)
		{
			registry = beanContext.getService(((IBeanConfiguration) registry).getName(), !optional);
			hasForeignContextBeenUsed = true;
		}
		else if (registry == null)
		{
			registry = beanContext.getService(registryBeanAutowiredType, !optional);
			hasForeignContextBeenUsed = true;
		}
		if (registry == null)
		{
			return null;
		}
		if (!hasForeignContextBeenUsed)
		{
			throw new LinkException(ILinkRegistryNeededConfiguration.class.getSimpleName()
					+ ".toContext(...) has been called but at the same time the registry has been provided as an instance with the .to(...) overload", this);
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
	public boolean link()
	{
		if (linked)
		{
			return false;
		}
		Object registry = null, listener = null;
		try
		{
			registry = resolveRegistry();
			if (registry == null)
			{
				return false;
			}
			listener = resolveListener();
			if (listener == null)
			{
				return false;
			}
			handleLink(registry, listener);
		}
		catch (Throwable e)
		{
			if (declarationStackTrace != null)
			{
				throw new LinkException("An error occured while trying to link " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener)
						+ " to '" + registry + "'\n" + Arrays.toString(declarationStackTrace), e, this);
			}
			throw new LinkException("An error occured while trying to link " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener) + " to '"
					+ registry + "'", e, this);
		}
		linked = true;
		linkCounter++;
		if (foreignBeanContext != null)
		{
			foreignBeanContext.registerDisposable(new LinkDisposable(this));
		}
		return true;
	}

	@Override
	public boolean unlink()
	{
		if (!linked)
		{
			return false;
		}
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
				return false;
			}
			handleUnlink(registry, listener);
		}
		catch (Exception e)
		{
			throw new LinkException("An error occured while trying to unlink " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener)
					+ " from '" + registry + "'", e, this);
		}
		linked = false;
		return true;
	}

	protected abstract ILogger getLog();

	protected abstract void handleLink(Object registry, Object listener) throws Exception;

	protected abstract void handleUnlink(Object registry, Object listener) throws Exception;
}
