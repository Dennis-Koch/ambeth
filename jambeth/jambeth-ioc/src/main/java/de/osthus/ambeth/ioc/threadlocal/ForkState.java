package de.osthus.ambeth.ioc.threadlocal;

import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;

public class ForkState extends ReentrantLock implements IForkState
{
	private static final long serialVersionUID = 3277389225453647471L;

	protected final ForkStateEntry[] forkStateEntries;

	protected final IForkedValueResolver[] forkedValueResolvers;

	protected final ArrayList<Object>[] forkedValues;

	@SuppressWarnings("unchecked")
	public ForkState(ForkStateEntry[] forkStateEntries, IForkedValueResolver[] forkedValueResolvers)
	{
		this.forkStateEntries = forkStateEntries;
		this.forkedValueResolvers = forkedValueResolvers;
		forkedValues = new ArrayList[forkStateEntries.length];
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
			Object forkedValue = forkedValueResolvers[a].createForkedValue();
			tlHandle.set(forkedValue);
		}
		return oldValues;
	}

	@SuppressWarnings("unchecked")
	protected void restoreThreadLocals(Object[] oldValues)
	{
		ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
		ArrayList<Object>[] forkedValues = this.forkedValues;
		lock();
		try
		{
			for (int a = 0, size = forkStateEntries.length; a < size; a++)
			{
				ForkStateEntry forkStateEntry = forkStateEntries[a];
				ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntry.valueTL;
				Object forkedValue = tlHandle.get();
				tlHandle.set(oldValues[a]);
				IForkedValueResolver forkedValueResolver = forkedValueResolvers[a];
				if (!(forkedValueResolver instanceof ForkProcessorValueResolver))
				{
					continue;
				}
				ArrayList<Object> forkedValuesItem = forkedValues[a];
				if (forkedValuesItem == null)
				{
					forkedValuesItem = new ArrayList<Object>();
					forkedValues[a] = forkedValuesItem;
				}
				forkedValuesItem.add(forkedValue);
			}
		}
		finally
		{
			unlock();
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

	@Override
	public void reintegrateForkedValues()
	{
		ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
		ArrayList<Object>[] forkedValues = this.forkedValues;
		for (int a = 0, size = forkStateEntries.length; a < size; a++)
		{
			ForkStateEntry forkStateEntry = forkStateEntries[a];
			ArrayList<Object> forkedValuesItem = forkedValues[a];

			if (forkedValuesItem == null)
			{
				// nothing to do
				continue;
			}
			Object originalValue = forkedValueResolvers[a].getOriginalValue();
			for (int b = 0, sizeB = forkedValuesItem.size(); b < sizeB; b++)
			{
				Object forkedValue = forkedValuesItem.get(b);
				forkStateEntry.forkProcessor.returnForkedValue(originalValue, forkedValue);
			}
		}
	}
}
