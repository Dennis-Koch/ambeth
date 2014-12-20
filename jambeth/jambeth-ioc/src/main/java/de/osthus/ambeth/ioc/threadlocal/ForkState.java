package de.osthus.ambeth.ioc.threadlocal;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;

public class ForkState implements IForkState
{
	protected final ForkStateEntry[] forkStateEntries;

	protected final IForkedValueResolver[] forkedValueResolvers;

	public ForkState(ForkStateEntry[] forkStateEntries, IForkedValueResolver[] forkedValueResolvers)
	{
		this.forkStateEntries = forkStateEntries;
		this.forkedValueResolvers = forkedValueResolvers;
	}

	@SuppressWarnings("unchecked")
	protected Object[] setThreadLocals()
	{
		ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
		Object[] oldValues = new Object[forkedValueResolvers.length];
		for (int a = 0, size = forkStateEntries.length; a < size; a++)
		{
			ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
			oldValues[a] = tlHandle.get();
			Object forkedValue = forkedValueResolvers[a].getForkedValue();
			tlHandle.set(forkedValue);
		}
		return oldValues;
	}

	@SuppressWarnings("unchecked")
	protected void restoreThreadLocals(Object[] oldValues)
	{
		ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		for (int a = 0, size = forkStateEntries.length; a < size; a++)
		{
			ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
			Object oldValue = oldValues[a];
			if (oldValue == null)
			{
				tlHandle.remove();
			}
			else
			{
				tlHandle.set(oldValue);
			}
		}
	}

	@Override
	public void use(Runnable runnable)
	{
		Object[] oldValues = setThreadLocals();
		try
		{
			runnable.run();
		}
		finally
		{
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public void use(IBackgroundWorkerDelegate runnable)
	{
		Object[] oldValues = setThreadLocals();
		try
		{
			runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public <V> void use(IBackgroundWorkerParamDelegate<V> runnable, V arg)
	{
		Object[] oldValues = setThreadLocals();
		try
		{
			runnable.invoke(arg);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public <R> R use(IResultingBackgroundWorkerDelegate<R> runnable)
	{
		Object[] oldValues = setThreadLocals();
		try
		{
			return runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public <R, V> R use(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V arg)
	{
		Object[] oldValues = setThreadLocals();
		try
		{
			return runnable.invoke(arg);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			restoreThreadLocals(oldValues);
		}
	}
}
