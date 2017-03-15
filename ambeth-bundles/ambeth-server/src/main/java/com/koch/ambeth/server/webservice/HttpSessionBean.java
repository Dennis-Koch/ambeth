package com.koch.ambeth.server.webservice;

import java.lang.reflect.Method;

import javax.servlet.http.HttpSession;

import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class HttpSessionBean
		implements IFactoryBean, MethodInterceptor, IHttpSessionProvider, IThreadLocalCleanupBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Forkable
	protected final ThreadLocal<HttpSession> httpSessionStackTL = new ThreadLocal<HttpSession>();

	protected Object obj;

	@Override
	public void cleanupThreadLocal() {
		// intended blank
	}

	@Override
	public Object getObject() throws Throwable {
		if (obj != null) {
			return obj;
		}
		obj = proxyFactory.createProxy(getClass().getClassLoader(), HttpSession.class,
				new Class<?>[] {IHttpSessionProvider.class}, this);
		return obj;
	}

	@Override
	public HttpSession getCurrentHttpSession() {
		return httpSessionStackTL.get();
	}

	@Override
	public void setCurrentHttpSession(HttpSession httpSession) {
		httpSessionStackTL.set(httpSession);
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		if (AbstractSimpleInterceptor.finalizeMethod.equals(method)) {
			return null;
		}
		if (IHttpSessionProvider.class.isAssignableFrom(method.getDeclaringClass())) {
			return proxy.invoke(this, args);
		}
		HttpSession httpSession = getCurrentHttpSession();
		if (httpSession == null) {
			throw new IllegalStateException("No http session bound to this thread");
		}
		return proxy.invoke(httpSession, args);
	}
}
