package com.koch.ambeth.ioc.link;

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.config.BeanConfiguration;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.extendable.IExtendableRegistry;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.IProxyFactory;

public class LinkController implements ILinkController, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private static final Object[] emptyArgs = new Object[0];

	protected IExtendableRegistry extendableRegistry;

	protected IProxyFactory proxyFactory;

	protected IProperties props;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(extendableRegistry, "extendableRegistry");
		ParamChecker.assertNotNull(props, "props");
		ParamChecker.assertNotNull(proxyFactory, "proxyFactory");
	}

	public void setExtendableRegistry(IExtendableRegistry extendableRegistry) {
		this.extendableRegistry = extendableRegistry;
	}

	public void setProps(IProperties props) {
		this.props = props;
	}

	public void setProxyFactory(IProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext,
			String listenerBeanName) {
		return link(serviceContext, listenerBeanName, (String) null);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, String listenerBeanName,
			String methodName) {
		LinkRuntime<Object> linkRuntime =
				new LinkRuntime<>((ServiceContext) serviceContext, LinkContainer.class);
		linkRuntime.listener(listenerBeanName);
		if (methodName != null) {
			linkRuntime.listenerMethod(methodName);
		}
		return linkRuntime;
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext,
			IBeanConfiguration listenerBean) {
		return link(serviceContext, listenerBean, (String) null);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext,
			IBeanConfiguration listenerBean, String methodName) {
		LinkRuntime<Object> linkRuntime =
				new LinkRuntime<>((ServiceContext) serviceContext, LinkContainer.class);
		linkRuntime.listener(listenerBean);
		if (methodName != null) {
			linkRuntime.listenerMethod(methodName);
		}
		return linkRuntime;
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, Object listener,
			String methodName) {
		if (listener instanceof String) {
			return link(serviceContext, (String) listener);
		}
		else if (listener instanceof IBeanConfiguration) {
			return link(serviceContext, (IBeanConfiguration) listener);
		}
		LinkRuntime<Object> linkRuntime =
				new LinkRuntime<>((ServiceContext) serviceContext, LinkContainer.class);
		linkRuntime.listener(listener);
		if (methodName != null) {
			linkRuntime.listenerMethod(methodName);
		}
		return linkRuntime;
	}

	@Override
	public <D> ILinkRegistryNeededRuntime<D> link(IServiceContext serviceContext, D listener) {
		LinkRuntime<D> linkRuntime =
				new LinkRuntime<>((ServiceContext) serviceContext, LinkContainer.class);
		linkRuntime.listener(listener);
		return linkRuntime;
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(String listenerBeanName,
			String methodName) {
		LinkConfiguration<Object> linkConfiguration =
				new LinkConfiguration<>(LinkContainer.class, proxyFactory, props);
		linkConfiguration.propertyValue(AbstractLinkContainer.PROPERTY_LISTENER_NAME, listenerBeanName);
		if (methodName != null) {
			linkConfiguration.propertyValue(AbstractLinkContainer.PROPERTY_LISTENER_METHOD_NAME,
					methodName);
		}
		return linkConfiguration;
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(IBeanConfiguration listenerBean,
			String methodName) {
		LinkConfiguration<Object> linkConfiguration =
				new LinkConfiguration<>(LinkContainer.class, proxyFactory, props);
		linkConfiguration.propertyValue(AbstractLinkContainer.PROPERTY_LISTENER_BEAN, listenerBean);
		if (methodName != null) {
			linkConfiguration.propertyValue(AbstractLinkContainer.PROPERTY_LISTENER_METHOD_NAME,
					methodName);
		}
		return linkConfiguration;
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(Object listener, String methodName) {
		if (listener instanceof String) {
			return createLinkConfiguration((String) listener, methodName);
		}
		else if (listener instanceof IBeanConfiguration) {
			return createLinkConfiguration((IBeanConfiguration) listener, methodName);
		}
		// else if (listener instanceof Delegate)
		// {
		// throw new Exception("Illegal state: Delegate can not have an additional methodName");
		// }
		LinkConfiguration<Object> linkConfiguration =
				new LinkConfiguration<>(LinkContainer.class, proxyFactory, props);
		linkConfiguration.propertyValue(AbstractLinkContainer.PROPERTY_LISTENER, listener);
		if (methodName != null) {
			linkConfiguration.propertyValue(AbstractLinkContainer.PROPERTY_LISTENER_METHOD_NAME,
					methodName);
		}
		return linkConfiguration;
	}

	@Override
	public <D> LinkConfiguration<D> createLinkConfiguration(D listener) {
		LinkConfiguration<D> linkConfiguration =
				new LinkConfiguration<>(LinkContainer.class, proxyFactory, props);
		linkConfiguration.propertyValue(AbstractLinkContainer.PROPERTY_LISTENER, listener);
		return linkConfiguration;
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName,
			Class<?> registryClass, Object... arguments) {
		ParamChecker.assertParamNotNull(serviceContext, "serviceContext");
		ParamChecker.assertParamNotNull(registryBeanName, "registryBeanName");
		ParamChecker.assertParamNotNull(listenerBeanName, "listenerBeanName");
		ParamChecker.assertParamNotNull(registryClass, "registryClass");
		ParamChecker.assertParamNotNull(arguments, "arguments");

		int expectedParamCount = arguments.length + 1;

		Method[] methods = extendableRegistry.getAddRemoveMethods(registryClass, arguments, null);

		Object[] realArguments = new Object[expectedParamCount];
		System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

		LinkContainerOld listenerContainer = new LinkContainerOld();
		listenerContainer.setRegistryBeanName(registryBeanName);
		listenerContainer.setListenerBeanName(listenerBeanName);
		listenerContainer.setAddMethod(methods[0]);
		listenerContainer.setRemoveMethod(methods[1]);
		listenerContainer.setArguments(realArguments);
		listenerContainer.setBeanContext(serviceContext);

		serviceContext.registerWithLifecycle(listenerContainer);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName,
			Class<?> registryClass) {
		link(serviceContext, registryBeanName, listenerBeanName, registryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, IBeanConfiguration listenerBean,
			Class<?> autowiredRegistryClass) {
		link(serviceContext, listenerBean, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, IBeanConfiguration listenerBean,
			Class<?> autowiredRegistryClass, Object... arguments) {
		ParamChecker.assertParamNotNull(serviceContext, "serviceContext");
		ParamChecker.assertParamNotNull(listenerBean, "listenerBean");
		ParamChecker.assertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
		ParamChecker.assertParamNotNull(arguments, "arguments");

		int expectedParamCount = arguments.length + 1;

		Method[] methods =
				extendableRegistry.getAddRemoveMethods(autowiredRegistryClass, arguments, null);

		Object[] realArguments = new Object[expectedParamCount];
		System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

		LinkContainerOld listenerContainer = new LinkContainerOld();
		listenerContainer.setRegistryBeanAutowiredType(autowiredRegistryClass);
		listenerContainer.setListenerBean(listenerBean);
		listenerContainer.setAddMethod(methods[0]);
		listenerContainer.setRemoveMethod(methods[1]);
		listenerContainer.setArguments(realArguments);
		listenerContainer.setBeanContext(serviceContext);

		serviceContext.registerWithLifecycle(listenerContainer);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String listenerBeanName,
			Class<?> autowiredRegistryClass) {
		link(serviceContext, listenerBeanName, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String listenerBeanName,
			Class<?> autowiredRegistryClass, Object... arguments) {
		ParamChecker.assertParamNotNull(serviceContext, "serviceContext");
		ParamChecker.assertParamNotNull(listenerBeanName, "listenerBeanName");
		ParamChecker.assertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
		ParamChecker.assertParamNotNull(arguments, "arguments");

		int expectedParamCount = arguments.length + 1;

		Method[] methods =
				extendableRegistry.getAddRemoveMethods(autowiredRegistryClass, arguments, null);

		Object[] realArguments = new Object[expectedParamCount];
		System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

		LinkContainerOld listenerContainer = new LinkContainerOld();
		listenerContainer.setRegistryBeanAutowiredType(autowiredRegistryClass);
		listenerContainer.setListenerBeanName(listenerBeanName);
		listenerContainer.setAddMethod(methods[0]);
		listenerContainer.setRemoveMethod(methods[1]);
		listenerContainer.setArguments(realArguments);
		listenerContainer.setBeanContext(serviceContext);

		serviceContext.registerWithLifecycle(listenerContainer);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String registryBeanName,
			String listenerBeanName, Class<?> registryClass) {
		return createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String registryBeanName,
			String listenerBeanName, Class<?> registryClass, Object... arguments) {
		ParamChecker.assertParamNotNull(registryBeanName, "registryBeanName");
		ParamChecker.assertParamNotNull(listenerBeanName, "listenerBeanName");
		ParamChecker.assertParamNotNull(registryClass, "registryClass");
		ParamChecker.assertParamNotNull(arguments, "arguments");

		int expectedParamCount = arguments.length + 1;

		Method[] methods = extendableRegistry.getAddRemoveMethods(registryClass, arguments, null);

		Object[] realArguments = new Object[expectedParamCount];
		System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

		BeanConfiguration beanConfiguration =
				new BeanConfiguration(LinkContainerOld.class, null, proxyFactory, props);
		beanConfiguration.propertyValue("RegistryBeanName", registryBeanName);
		beanConfiguration.propertyValue("ListenerBeanName", listenerBeanName);
		beanConfiguration.propertyValue("AddMethod", methods[0]);
		beanConfiguration.propertyValue("RemoveMethod", methods[1]);
		beanConfiguration.propertyValue("Arguments", realArguments);
		return beanConfiguration;
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String listenerBeanName,
			Class<?> autowiredRegistryClass) {
		return createLinkConfiguration(listenerBeanName, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String listenerBeanName,
			Class<?> autowiredRegistryClass, Object... arguments) {
		ParamChecker.assertParamNotNull(listenerBeanName, "listenerBeanName");
		ParamChecker.assertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
		ParamChecker.assertParamNotNull(arguments, "arguments");

		int expectedParamCount = arguments.length + 1;

		Method[] methods =
				extendableRegistry.getAddRemoveMethods(autowiredRegistryClass, arguments, null);

		Object[] realArguments = new Object[expectedParamCount];
		System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

		BeanConfiguration beanConfiguration =
				new BeanConfiguration(LinkContainerOld.class, null, proxyFactory, props);
		beanConfiguration.propertyValue("RegistryBeanAutowiredType", autowiredRegistryClass);
		beanConfiguration.propertyValue("ListenerBeanName", listenerBeanName);
		beanConfiguration.propertyValue("AddMethod", methods[0]);
		beanConfiguration.propertyValue("RemoveMethod", methods[1]);
		beanConfiguration.propertyValue("Arguments", realArguments);
		return beanConfiguration;
	}
}
