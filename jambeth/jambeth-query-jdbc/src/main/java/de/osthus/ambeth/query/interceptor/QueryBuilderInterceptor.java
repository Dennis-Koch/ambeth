package de.osthus.ambeth.query.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.ISqlJoin;

public class QueryBuilderInterceptor implements MethodInterceptor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected static final Method disposeMethod;

	protected static final HashSet<Method> cleanupMethods = new HashSet<Method>();

	protected IQueryBuilder<?> queryBuilder;

	protected boolean finalized = false;

	static
	{
		try
		{
			disposeMethod = IQueryBuilder.class.getMethod("dispose");
			cleanupMethods.add(IQueryBuilder.class.getMethod("build"));
			cleanupMethods.add(IQueryBuilder.class.getMethod("build", IOperand.class));
			cleanupMethods.add(IQueryBuilder.class.getMethod("build", IOperand.class, ISqlJoin[].class));
			cleanupMethods.add(disposeMethod);
		}
		catch (SecurityException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		catch (NoSuchMethodException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public QueryBuilderInterceptor(IQueryBuilder<?> queryBuilder)
	{
		this.queryBuilder = queryBuilder;
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (queryBuilder != null)
		{
			queryBuilder.dispose();
		}
	}

	@Override
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
		if (this.finalized)
		{
			if (disposeMethod.equals(method))
			{
				return null;
			}
			throw new IllegalStateException("This query builder already is finalized!");
		}
		try
		{
			Object result = methodProxy.invoke(this.queryBuilder, args);
			if (result == this.queryBuilder)
			{
				return proxy;
			}
			return result;
		}
		finally
		{
			if (cleanupMethods.contains(method))
			{
				this.finalized = true;
				IQueryBuilder<?> queryBuilder = this.queryBuilder;
				if (queryBuilder != null)
				{
					this.queryBuilder = null;
					queryBuilder.dispose();
				}
			}
		}
	}
}
