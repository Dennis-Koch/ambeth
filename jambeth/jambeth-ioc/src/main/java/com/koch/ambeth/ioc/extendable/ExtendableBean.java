package com.koch.ambeth.ioc.extendable;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.accessor.AccessorClassLoader;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.exception.ExtendableException;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;

public class ExtendableBean extends AbstractSimpleInterceptor
		implements IFactoryBean, IInitializingBean {
	public static final String P_PROVIDER_TYPE = "ProviderType";

	public static final String P_EXTENDABLE_TYPE = "ExtendableType";

	public static final String P_DEFAULT_BEAN = "DefaultBean";

	protected static final Object[] emptyArgs = new Object[0];

	protected static final Object[] oneArgs = new Object[] {new Object()};

	protected static final Class<?>[] classObjectArgs = new Class[] {Object.class};

	public static IBeanConfiguration registerExtendableBean(IBeanContextFactory beanContextFactory,
			Class<?> providerType, Class<?> extendableType, ClassLoader classLoader) {
		return registerExtendableBean(beanContextFactory, null, providerType, extendableType,
				classLoader);
	}

	public static IBeanConfiguration registerExtendableBean(IBeanContextFactory beanContextFactory,
			String beanName, Class<?> providerType, Class<?> extendableType, ClassLoader classLoader) {
		if (beanName != null) {
			return beanContextFactory.registerBean(beanName, ExtendableBean.class)
					.propertyValue(ExtendableBean.P_PROVIDER_TYPE, providerType)
					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, extendableType)
					.autowireable(providerType, extendableType);
		}
		return beanContextFactory.registerBean(ExtendableBean.class)
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, providerType)
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, extendableType)
				.autowireable(providerType, extendableType);
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IExtendableRegistry extendableRegistry;

	@Autowired
	protected IProxyFactory proxyFactory;

	protected Class<?> providerType;

	protected Class<?> extendableType;

	protected ClassLoader classLoader;

	protected Object extendableContainer;

	protected Object defaultBean = null;

	protected final HashMap<Method, Method> methodMap = new HashMap<>(0.5f);

	protected Object proxy;

	protected boolean allowMultiValue = false;

	protected Class<?>[] argumentTypes = null;

	protected Method providerTypeGetOne = null;

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(providerType, "ProviderType");
		ParamChecker.assertNotNull(extendableType, "ExtendableType");

		Method[] addRemoveMethods;
		if (argumentTypes != null) {
			addRemoveMethods = extendableRegistry.getAddRemoveMethods(extendableType, argumentTypes);
		}
		else {
			addRemoveMethods = extendableRegistry.getAddRemoveMethods(extendableType);
		}
		Method addMethod = addRemoveMethods[0];
		Method removeMethod = addRemoveMethods[1];

		Class[] parameterTypes = addMethod.getParameterTypes();
		Class extensionType = parameterTypes[0];

		if (parameterTypes.length == 1) {
			extendableContainer = new DefaultExtendableContainer<>(extensionType, "message");

			Method registerMethod = extendableContainer.getClass().getMethod("register", classObjectArgs);
			Method unregisterMethod =
					extendableContainer.getClass().getMethod("unregister", classObjectArgs);
			Method getAllMethod = extendableContainer.getClass().getMethod("getExtensions");
			Method[] methodsOfProviderType = ReflectUtil.getMethods(providerType);

			methodMap.put(addMethod, registerMethod);
			methodMap.put(removeMethod, unregisterMethod);

			for (int a = methodsOfProviderType.length; a-- > 0;) {
				Method methodOfProviderType = methodsOfProviderType[a];
				if (methodOfProviderType.getParameterTypes().length == 0) {
					methodMap.put(methodOfProviderType, getAllMethod);
				}
			}
		}
		else if (parameterTypes.length == 2) {
			Class<?> keyType = parameterTypes[1];
			if (Class.class.equals(keyType)) {
				extendableContainer =
						new ClassExtendableContainer<>("message", "keyMessage", allowMultiValue);
			}
			else {
				keyType = Object.class;
				extendableContainer =
						new MapExtendableContainer<>("message", "keyMessage", allowMultiValue);
			}
			AccessorClassLoader classLoader = AccessorClassLoader.get(extendableContainer.getClass());
			FastClass fastClass = FastClass.create(classLoader, extendableContainer.getClass());
			Method registerMethod =
					extendableContainer.getClass().getMethod("register", Object.class, keyType);
			Method unregisterMethod =
					extendableContainer.getClass().getMethod("unregister", Object.class, keyType);
			Method getOneMethod = extendableContainer.getClass().getMethod("getExtension", keyType);
			Method getAllMethod = extendableContainer.getClass().getMethod("getExtensions");
			Method[] methodsOfProviderType = providerType.getMethods();

			methodMap.put(addMethod, registerMethod);
			methodMap.put(removeMethod, unregisterMethod);

			for (int a = methodsOfProviderType.length; a-- > 0;) {
				Method methodOfProviderType = methodsOfProviderType[a];
				if (methodOfProviderType.getParameterTypes().length == 1) {
					methodMap.put(methodOfProviderType, getOneMethod);
					providerTypeGetOne = methodOfProviderType;
				}
				else if (methodOfProviderType.getParameterTypes().length == 0) {
					methodMap.put(methodOfProviderType, getAllMethod);
				}
			}
		}
		else {
			throw new ExtendableException("ExtendableType '" + extendableType.getName()
					+ "' not supported: It must contain exactly 2 methods with each either 1 or 2 arguments");
		}
	}

	public void setProviderType(Class<?> providerType) {
		this.providerType = providerType;
	}

	public void setExtendableType(Class<?> extendableType) {
		this.extendableType = extendableType;
	}

	public void setAllowMultiValue(boolean allowMultiValue) {
		this.allowMultiValue = allowMultiValue;
	}

	public void setArgumentTypes(Class<?>[] argumentTypes) {
		this.argumentTypes = argumentTypes;
	}

	public void setDefaultBean(Object defaultBean) {
		this.defaultBean = defaultBean;
	}

	@Override
	public Object getObject() throws Throwable {
		if (proxy == null) {
			proxy = proxyFactory.createProxy(new Class[] {providerType, extendableType}, this);
		}
		return proxy;
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		Method mappedMethod = methodMap.get(method);
		if (mappedMethod == null) {
			return proxy.invoke(extendableContainer, args);
		}
		Object value = mappedMethod.invoke(extendableContainer, args);
		if (value == null && method.equals(providerTypeGetOne)) {
			value = defaultBean;
		}
		return value;
	}
}
