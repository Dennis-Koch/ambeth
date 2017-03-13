package com.koch.ambeth.ioc.extendable;

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.IInitializingBean;
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
import net.sf.cglib.reflect.FastMethod;

public class ExtendableBean extends AbstractSimpleInterceptor implements IFactoryBean, IInitializingBean
{
	public static final String P_PROVIDER_TYPE = "ProviderType";

	public static final String P_EXTENDABLE_TYPE = "ExtendableType";

	public static final String P_DEFAULT_BEAN = "DefaultBean";

	protected static final Object[] emptyArgs = new Object[0];

	protected static final Object[] oneArgs = new Object[] { new Object() };

	protected static final Class<?>[] classObjectArgs = new Class[] { Object.class };

	public static IBeanConfiguration registerExtendableBean(IBeanContextFactory beanContextFactory, Class<?> providerType, Class<?> extendableType)
	{
		return registerExtendableBean(beanContextFactory, null, providerType, extendableType);
	}

	public static IBeanConfiguration registerExtendableBean(IBeanContextFactory beanContextFactory, String beanName, Class<?> providerType,
			Class<?> extendableType)
	{
		if (beanName != null)
		{
			return beanContextFactory.registerBean(beanName, ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, providerType)
					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, extendableType).autowireable(providerType, extendableType);
		}
		return beanContextFactory.registerBean(ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, providerType)
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, extendableType).autowireable(providerType, extendableType);
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

	protected Object extendableContainer;

	protected Object defaultBean = null;

	protected final HashMap<Method, FastMethod> methodMap = new HashMap<Method, FastMethod>(0.5f);

	protected Object proxy;

	protected boolean allowMultiValue = false;

	protected Class<?>[] argumentTypes = null;

	protected Method providerTypeGetOne = null;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(providerType, "ProviderType");
		ParamChecker.assertNotNull(extendableType, "ExtendableType");

		FastMethod[] addRemoveMethods;
		if (argumentTypes != null)
		{
			addRemoveMethods = extendableRegistry.getAddRemoveMethods(extendableType, argumentTypes);
		}
		else
		{
			addRemoveMethods = extendableRegistry.getAddRemoveMethods(extendableType);
		}
		FastMethod addMethod = addRemoveMethods[0];
		FastMethod removeMethod = addRemoveMethods[1];

		Class[] parameterTypes = addMethod.getParameterTypes();
		Class extensionType = parameterTypes[0];

		if (parameterTypes.length == 1)
		{
			extendableContainer = new DefaultExtendableContainer<Object>(extensionType, "message");

			FastClass fastClass = FastClass.create(extendableContainer.getClass());
			FastMethod registerMethod = fastClass.getMethod("register", classObjectArgs);
			FastMethod unregisterMethod = fastClass.getMethod("unregister", classObjectArgs);
			FastMethod getAllMethod = fastClass.getMethod("getExtensions", null);
			Method[] methodsOfProviderType = ReflectUtil.getMethods(providerType);

			methodMap.put(addMethod.getJavaMethod(), registerMethod);
			methodMap.put(removeMethod.getJavaMethod(), unregisterMethod);

			for (int a = methodsOfProviderType.length; a-- > 0;)
			{
				Method methodOfProviderType = methodsOfProviderType[a];
				if (methodOfProviderType.getParameterTypes().length == 0)
				{
					methodMap.put(methodOfProviderType, getAllMethod);
				}
			}
		}
		else if (parameterTypes.length == 2)
		{
			Class<?> keyType = parameterTypes[1];
			if (Class.class.equals(keyType))
			{
				extendableContainer = new ClassExtendableContainer<Object>("message", "keyMessage", allowMultiValue);
			}
			else
			{
				keyType = Object.class;
				extendableContainer = new MapExtendableContainer<Object, Object>("message", "keyMessage", allowMultiValue);
			}
			FastClass fastClass = FastClass.create(extendableContainer.getClass());
			FastMethod registerMethod = fastClass.getMethod("register", new Class[] { Object.class, keyType });
			FastMethod unregisterMethod = fastClass.getMethod("unregister", new Class[] { Object.class, keyType });
			FastMethod getOneMethod = fastClass.getMethod("getExtension", new Class[] { keyType });
			FastMethod getAllMethod = fastClass.getMethod("getExtensions", null);
			Method[] methodsOfProviderType = providerType.getMethods();

			methodMap.put(addMethod.getJavaMethod(), registerMethod);
			methodMap.put(removeMethod.getJavaMethod(), unregisterMethod);

			for (int a = methodsOfProviderType.length; a-- > 0;)
			{
				Method methodOfProviderType = methodsOfProviderType[a];
				if (methodOfProviderType.getParameterTypes().length == 1)
				{
					methodMap.put(methodOfProviderType, getOneMethod);
					providerTypeGetOne = methodOfProviderType;
				}
				else if (methodOfProviderType.getParameterTypes().length == 0)
				{
					methodMap.put(methodOfProviderType, getAllMethod);
				}
			}
		}
		else
		{
			throw new ExtendableException("ExtendableType '" + extendableType.getName()
					+ "' not supported: It must contain exactly 2 methods with each either 1 or 2 arguments");
		}
	}

	public void setProviderType(Class<?> providerType)
	{
		this.providerType = providerType;
	}

	public void setExtendableType(Class<?> extendableType)
	{
		this.extendableType = extendableType;
	}

	public void setAllowMultiValue(boolean allowMultiValue)
	{
		this.allowMultiValue = allowMultiValue;
	}

	public void setArgumentTypes(Class<?>[] argumentTypes)
	{
		this.argumentTypes = argumentTypes;
	}

	public void setDefaultBean(Object defaultBean)
	{
		this.defaultBean = defaultBean;
	}

	@Override
	public Object getObject() throws Throwable
	{
		if (proxy == null)
		{
			proxy = proxyFactory.createProxy(new Class[] { providerType, extendableType }, this);
		}
		return proxy;
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		FastMethod mappedMethod = methodMap.get(method);
		if (mappedMethod == null)
		{
			return proxy.invoke(extendableContainer, args);
		}
		Object value = mappedMethod.invoke(extendableContainer, args);
		if (value == null && method.equals(providerTypeGetOne))
		{
			value = defaultBean;
		}
		return value;
	}
}
