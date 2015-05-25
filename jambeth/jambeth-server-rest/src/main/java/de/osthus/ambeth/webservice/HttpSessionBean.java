package de.osthus.ambeth.webservice;

import java.lang.reflect.Method;

import javax.servlet.http.HttpSession;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.proxy.IProxyFactory;

public class HttpSessionBean implements IFactoryBean, MethodInterceptor, IHttpSessionSetter, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Forkable
	protected final ThreadLocal<ArrayList<HttpSession>> httpSessionStackTL = new ThreadLocal<ArrayList<HttpSession>>();

	protected Object obj;

	@Override
	public void cleanupThreadLocal()
	{
		if (httpSessionStackTL.get() != null)
		{
			throw new IllegalStateException("Must never happen");
		}
	}

	@Override
	public Object getObject() throws Throwable
	{
		if (obj != null)
		{
			return obj;
		}
		obj = proxyFactory.createProxy(HttpSession.class, new Class<?>[] { IHttpSessionSetter.class }, this);
		return obj;
	}

	@Override
	public void setCurrentHttpSession(HttpSession httpSession)
	{
		ArrayList<HttpSession> httpSessionStack = httpSessionStackTL.get();
		if (httpSession != null)
		{
			if (httpSessionStack == null)
			{
				httpSessionStack = new ArrayList<HttpSession>(2);
				httpSessionStackTL.set(httpSessionStack);
			}
			httpSessionStack.add(httpSession);
			return;
		}
		if (httpSessionStack == null)
		{
			throw new IllegalStateException("No http session bound to this thread");
		}
		httpSessionStack.popLastElement();
		if (httpSessionStack.size() == 0)
		{
			httpSessionStackTL.set(null);
		}
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (AbstractSimpleInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
		if (IHttpSessionSetter.class.isAssignableFrom(method.getDeclaringClass()))
		{
			return proxy.invoke(this, args);
		}
		ArrayList<HttpSession> httpSessionStack = httpSessionStackTL.get();
		if (httpSessionStack == null)
		{
			throw new IllegalStateException("No http session bound to this thread");
		}
		return proxy.invoke(httpSessionStack.peek(), args);
	}
}
