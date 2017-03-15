package com.koch.ambeth.ioc.link;

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

/**
 * Please use LinkContainer instead
 */
@Deprecated
public class LinkContainerOld implements ILinkContainer, IInitializingBean {
	@LogInstance
	private ILogger log;

	protected Object[] arguments;

	protected IBeanConfiguration listenerBean;

	protected Class<?> registryBeanAutowiredType;
	protected String listenerBeanName;
	protected String registryBeanName;
	protected Method addMethod;
	protected Method removeMethod;
	protected IServiceContext beanContext;
	protected Object registry;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertTrue(registryBeanAutowiredType != null || registryBeanName != null,
				"either property 'registryBeanAutowiredType' or 'registryBeanName' must be valid");
		ParamChecker.assertTrue(listenerBean != null || listenerBeanName != null,
				"either property 'listenerBean' or 'listenerBeanName' must be valid");
		ParamChecker.assertParamNotNull(addMethod, "addMethod");
		ParamChecker.assertParamNotNull(removeMethod, "removeMethod");
		ParamChecker.assertParamNotNull(arguments, "arguments");
		ParamChecker.assertParamNotNull(beanContext, "beanContext");
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public void setListenerBeanName(String listenerBeanName) {
		this.listenerBeanName = listenerBeanName;
	}

	public void setRegistryBeanName(String registryBeanName) {
		this.registryBeanName = registryBeanName;
	}

	public void setListenerBean(IBeanConfiguration listenerBean) {
		this.listenerBean = listenerBean;
	}

	public void setRegistryBeanAutowiredType(Class<?> registryBeanAutowiredType) {
		this.registryBeanAutowiredType = registryBeanAutowiredType;
	}

	public void setAddMethod(Method addMethod) {
		this.addMethod = addMethod;
	}

	public void setRemoveMethod(Method removeMethod) {
		this.removeMethod = removeMethod;
	}

	public void setBeanContext(IServiceContext beanContext) {
		this.beanContext = beanContext;
	}

	protected Object resolveRegistry() {
		if (registryBeanName != null) {
			Object registry = beanContext.getService(registryBeanName);
			if (registry == null) {
				throw new IllegalStateException(
						"No registry bean with name '" + registryBeanName + "' found to link bean");
			}
			return registry;
		}
		Object registry = beanContext.getService(registryBeanAutowiredType);
		if (registry == null) {
			throw new IllegalStateException("No registry bean with autowired type "
					+ registryBeanAutowiredType.getName() + " found to link bean");
		}
		return registry;
	}

	@Override
	public boolean link() {
		if (registry != null) {
			throw new IllegalStateException();
		}
		registry = resolveRegistry();

		Object listener = null;
		if (listenerBeanName == null) {
			listenerBeanName = listenerBean.getName();
			if (listenerBeanName == null) {
				listener = listenerBean.getInstance();
			}
		}
		if (listenerBeanName != null) {
			listener = beanContext.getService(listenerBeanName);
		}
		ParamChecker.assertParamNotNull(listener, "listener");
		try {
			arguments[0] = listener;
			addMethod.invoke(registry, arguments);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, "Attempt failed to register '" + listener + "' to '"
					+ registry + "' with method " + addMethod);
		}
		return true;
	}

	@Override
	public boolean unlink() {
		if (registry == null) {
			// Nothing to do because there was no (successful) call to link() before
			if (log.isDebugEnabled()) {
				log.debug(
						"Unlink has been called without prior linking. If no other exception is visible in the logs then this may be a bug");
			}
			return false;
		}
		Object listener = null;
		try {
			if (listenerBeanName == null) {
				listenerBeanName = listenerBean.getName();
				if (listenerBeanName == null) {
					listener = listenerBean.getInstance();
				}
			}
			if (listenerBeanName != null) {
				listener = beanContext.getService(listenerBeanName);
			}
			ParamChecker.assertParamNotNull(listener, "listener");
			arguments[0] = listener;
			removeMethod.invoke(registry, arguments);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, "Attempt failed to unregister '" + listener + "' from '"
					+ registry + "' with method " + removeMethod);
		}
		finally {
			registry = null;
		}
		return true;
	}
}
