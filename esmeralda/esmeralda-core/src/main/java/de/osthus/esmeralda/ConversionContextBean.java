package de.osthus.esmeralda;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.util.ReflectUtil;

public class ConversionContextBean implements IThreadLocalCleanupBean, MethodInterceptor, IFactoryBean, IInitializingBean
{
	private static final java.lang.reflect.Method finalizeMethod = CascadedInterceptor.finalizeMethod;

	private static final java.lang.reflect.Method toStringMethod;

	private static final java.lang.reflect.Method setCurrentMethod;

	private static final java.lang.reflect.Method getCurrentMethod;

	static
	{
		setCurrentMethod = ReflectUtil.getDeclaredMethod(false, IConversionContext.class, void.class, "setCurrent", IConversionContext.class);
		getCurrentMethod = ReflectUtil.getDeclaredMethod(false, IConversionContext.class, IConversionContext.class, "getCurrent");
		toStringMethod = ReflectUtil.getDeclaredMethod(false, Object.class, String.class, "toString");
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IProxyFactory proxyFactory;

	protected final ThreadLocal<IConversionContext> conversionContextTL = new ThreadLocal<IConversionContext>();

	private IConversionContext proxy;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		proxy = proxyFactory.createProxy(IConversionContext.class, this);
	}

	@Override
	public Object getObject() throws Throwable
	{
		return proxy;
	}

	@Override
	public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			return null;
		}
		if (setCurrentMethod.equals(method))
		{
			setCurrent((IConversionContext) args[0]);
			return null;
		}
		if (getCurrentMethod.equals(method))
		{
			return getCurrent();
		}
		if (toStringMethod.equals(method))
		{
			return toString();
		}
		if (IConversionContext.class.isAssignableFrom(method.getDeclaringClass()))
		{
			IConversionContext context = getContext();
			return proxy.invoke(context, args);
		}
		return proxy.invoke(this, args);
	}

	@Override
	public void cleanupThreadLocal()
	{
		conversionContextTL.remove();
	}

	protected IConversionContext getContext()
	{
		return conversionContextTL.get();
	}

	protected IConversionContext getCurrent()
	{
		return getContext();
	}

	protected void setCurrent(IConversionContext current)
	{
		if (current == null)
		{
			conversionContextTL.remove();
		}
		else
		{
			conversionContextTL.set(current);
		}
	}

	@Override
	public String toString()
	{
		IConversionContext context = getContext();
		if (context == null)
		{
			return super.toString();
		}
		return context.toString();
	}
}
