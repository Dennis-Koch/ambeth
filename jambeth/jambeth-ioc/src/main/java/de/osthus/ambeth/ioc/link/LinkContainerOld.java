package de.osthus.ambeth.ioc.link;

import net.sf.cglib.reflect.FastMethod;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

/**
 * Please use LinkContainer instead
 */
@Deprecated
public class LinkContainerOld implements ILinkContainer, IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected Object[] arguments;

	protected IBeanConfiguration listenerBean;

	protected Class<?> registryBeanAutowiredType;
	protected String listenerBeanName;
	protected String registryBeanName;
	protected FastMethod addMethod;
	protected FastMethod removeMethod;
	protected IServiceContext beanContext;
	protected Object registry;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertTrue(registryBeanAutowiredType != null || registryBeanName != null,
				"either property 'registryBeanAutowiredType' or 'registryBeanName' must be valid");
		ParamChecker.assertTrue(listenerBean != null || listenerBeanName != null, "either property 'listenerBean' or 'listenerBeanName' must be valid");
		ParamChecker.assertParamNotNull(addMethod, "addMethod");
		ParamChecker.assertParamNotNull(removeMethod, "removeMethod");
		ParamChecker.assertParamNotNull(arguments, "arguments");
		ParamChecker.assertParamNotNull(beanContext, "beanContext");
	}

	public void setArguments(Object[] arguments)
	{
		this.arguments = arguments;
	}

	public void setListenerBeanName(String listenerBeanName)
	{
		this.listenerBeanName = listenerBeanName;
	}

	public void setRegistryBeanName(String registryBeanName)
	{
		this.registryBeanName = registryBeanName;
	}

	public void setListenerBean(IBeanConfiguration listenerBean)
	{
		this.listenerBean = listenerBean;
	}

	public void setRegistryBeanAutowiredType(Class<?> registryBeanAutowiredType)
	{
		this.registryBeanAutowiredType = registryBeanAutowiredType;
	}

	public void setAddMethod(FastMethod addMethod)
	{
		this.addMethod = addMethod;
	}

	public void setRemoveMethod(FastMethod removeMethod)
	{
		this.removeMethod = removeMethod;
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	protected Object resolveRegistry()
	{
		if (registryBeanName != null)
		{
			Object registry = beanContext.getService(registryBeanName);
			if (registry == null)
			{
				throw new IllegalStateException("No registry bean with name '" + registryBeanName + "' found to link bean");
			}
			return registry;
		}
		Object registry = beanContext.getService(registryBeanAutowiredType);
		if (registry == null)
		{
			throw new IllegalStateException("No registry bean with autowired type " + registryBeanAutowiredType.getName() + " found to link bean");
		}
		return registry;
	}

	@Override
	public void link()
	{
		if (registry != null)
		{
			throw new IllegalStateException();
		}
		registry = resolveRegistry();

		Object listener = null;
		if (listenerBeanName == null)
		{
			listenerBeanName = listenerBean.getName();
			if (listenerBeanName == null)
			{
				listener = listenerBean.getInstance();
			}
		}
		if (listenerBeanName != null)
		{
			listener = beanContext.getService(listenerBeanName);
		}
		ParamChecker.assertParamNotNull(listener, "listener");
		try
		{
			arguments[0] = listener;
			this.addMethod.invoke(registry, arguments);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Attempt failed to register '" + listener + "' to '" + registry + "' with method " + addMethod);
		}
	}

	@Override
	public void unlink()
	{
		if (registry == null)
		{
			// Nothing to do because there was no (successful) call to link() before
			if (log.isDebugEnabled())
			{
				log.debug("Unlink has been called without prior linking. If no other exception is visible in the logs then this may be a bug");
			}
			return;
		}
		Object listener = null;
		try
		{
			if (listenerBeanName == null)
			{
				listenerBeanName = listenerBean.getName();
				if (listenerBeanName == null)
				{
					listener = listenerBean.getInstance();
				}
			}
			if (listenerBeanName != null)
			{
				listener = beanContext.getService(listenerBeanName);
			}
			ParamChecker.assertParamNotNull(listener, "listener");
			arguments[0] = listener;
			this.removeMethod.invoke(registry, arguments);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Attempt failed to unregister '" + listener + "' from '" + registry + "' with method " + removeMethod);
		}
		finally
		{
			registry = null;
		}
	}
}
