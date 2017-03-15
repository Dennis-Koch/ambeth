package com.koch.ambeth.ioc.proxy;

import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.MethodInterceptor;

public class ProxyBean implements IInitializingBean, IFactoryBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IProxyFactory proxyFactory;

	protected Class<?> type;

	protected Class<?>[] additionalTypes;

	protected MethodInterceptor interceptor;

	protected Object proxy;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(proxyFactory, "ProxyFactory");
		ParamChecker.assertNotNull(type, "Type");

		getProxy();
	}

	public void setAdditionalTypes(Class<?>[] additionalTypes) {
		this.additionalTypes = additionalTypes;
	}

	public void setInterceptor(MethodInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	public void setProxyFactory(IProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	protected Object getProxy() {
		if (proxy == null) {
			if (interceptor != null) {
				if (additionalTypes != null) {
					proxy = proxyFactory.createProxy(getClass().getClassLoader(), type, additionalTypes,
							interceptor);
				}
				else {
					proxy = proxyFactory.createProxy(getClass().getClassLoader(), type, interceptor);
				}
			}
			else if (additionalTypes != null) {
				proxy = proxyFactory.createProxy(getClass().getClassLoader(), type, additionalTypes);
			}
			else {
				proxy = proxyFactory.createProxy(getClass().getClassLoader(), type);
			}
		}
		return proxy;
	}

	@Override
	public Object getObject() {
		return getProxy();
	}
}
