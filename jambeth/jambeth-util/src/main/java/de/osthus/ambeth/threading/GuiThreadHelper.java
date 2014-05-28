package de.osthus.ambeth.threading;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class GuiThreadHelper implements IGuiThreadHelper
{
	@Override
	public boolean isInGuiThread()
	{
		return false;
	}

	@Override
	public void invokeInGuiAndWait(IBackgroundWorkerDelegate runnable)
	{
		try
		{
			runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public <R> R invokeInGuiAndWait(IResultingBackgroundWorkerDelegate<R> runnable)
	{
		try
		{
			return runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public <R, S> R invokeInGuiAndWait(IResultingBackgroundWorkerParamDelegate<R, S> runnable, S state)
	{
		try
		{
			return runnable.invoke(state);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void invokeInGuiAndWait(ISendOrPostCallback runnable, Object state)
	{
		try
		{
			runnable.invoke(state);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void invokeInGui(IBackgroundWorkerDelegate runnable)
	{
		try
		{
			runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void invokeInGui(ISendOrPostCallback runnable, Object state)
	{
		try
		{
			runnable.invoke(state);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void invokeInGuiLate(IBackgroundWorkerDelegate runnable)
	{
		try
		{
			runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void invokeInGuiLate(ISendOrPostCallback runnable, Object state)
	{
		try
		{
			runnable.invoke(state);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void invokeOutOfGui(IBackgroundWorkerDelegate runnable)
	{
		try
		{
			runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
