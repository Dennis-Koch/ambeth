package de.osthus.ambeth.webservice;

import java.lang.reflect.Method;

import javax.servlet.http.HttpSession;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.proxy.IProxyFactory;

public class HttpSessionBean implements IFactoryBean, MethodInterceptor, IHttpSessionProvider, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Forkable
	protected final ThreadLocal<HttpSession> httpSessionStackTL = new ThreadLocal<HttpSession>();

	protected Object obj;

	@Override
	public void cleanupThreadLocal()
	{
		// intended blank
	}

	@Override
	public Object getObject() throws Throwable
	{
		if (obj != null)
		{
			return obj;
		}
		obj = proxyFactory.createProxy(HttpSession.class, new Class<?>[] { IHttpSessionProvider.class }, this);
		return obj;
	}

	@Override
	public HttpSession getCurrentHttpSession()
	{
		return httpSessionStackTL.get();
	}

	@Override
	public void setCurrentHttpSession(HttpSession httpSession)
	{
		httpSessionStackTL.set(httpSession);
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (AbstractSimpleInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
		if (IHttpSessionProvider.class.isAssignableFrom(method.getDeclaringClass()))
		{
			return proxy.invoke(this, args);
		}
		HttpSession httpSession = getCurrentHttpSession();
		if (httpSession == null)
		{
			throw new IllegalStateException("No http session bound to this thread");
		}
		return proxy.invoke(httpSession, args);
	}
}
