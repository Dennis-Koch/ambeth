package de.osthus.ambeth.threading;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.util.ParamHolder;
import de.osthus.ambeth.util.ReflectUtil;

public class GuiThreadHelper implements IGuiThreadHelper
{
	private static final Field dispatchThread;

	static
	{
		Field f_dispatchThread;
		try
		{
			f_dispatchThread = ReflectUtil.getDeclaredField(Toolkit.class, "toolkit");
			// f_dispatchThread = ReflectUtil.getDeclaredField(EventQueue.class, "dispatchThread");
		}
		catch (Throwable e)
		{
			f_dispatchThread = null;
		}
		dispatchThread = f_dispatchThread;
	}

	protected Executor executor;

	protected boolean isGuiInitialized, skipGuiInitializeCheck;

	public void setExecutor(Executor executor)
	{
		this.executor = executor;
	}

	protected boolean isGuiInitialized()
	{
		if (!isGuiInitialized && dispatchThread != null && !skipGuiInitializeCheck)
		{
			try
			{
				isGuiInitialized = dispatchThread.get(null) != null;
			}
			catch (Throwable e)
			{
				skipGuiInitializeCheck = true;
			}
		}
		return isGuiInitialized;
	}

	@Override
	public boolean isInGuiThread()
	{
		return isGuiInitialized() ? EventQueue.isDispatchThread() : false;
	}

	@Override
	public void invokeInGuiAndWait(final IBackgroundWorkerDelegate runnable)
	{
		if (!isGuiInitialized() || EventQueue.isDispatchThread())
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
		else
		{
			try
			{
				EventQueue.invokeAndWait(new Runnable()
				{
					@Override
					public void run()
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
				});
			}
			catch (InvocationTargetException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (InterruptedException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public <R> R invokeInGuiAndWait(final IResultingBackgroundWorkerDelegate<R> runnable)
	{
		if (!isGuiInitialized() || EventQueue.isDispatchThread())
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
		else
		{
			final ParamHolder<R> ph = new ParamHolder<R>();
			try
			{
				EventQueue.invokeAndWait(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							ph.setValue(runnable.invoke());
						}
						catch (Throwable e)
						{
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				});
				return ph.getValue();
			}
			catch (InvocationTargetException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (InterruptedException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public <R, S> R invokeInGuiAndWait(final IResultingBackgroundWorkerParamDelegate<R, S> runnable, final S state)
	{
		if (!isGuiInitialized() || EventQueue.isDispatchThread())
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
		else
		{
			final ParamHolder<R> ph = new ParamHolder<R>();
			try
			{
				EventQueue.invokeAndWait(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							ph.setValue(runnable.invoke(state));
						}
						catch (Throwable e)
						{
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				});
				return ph.getValue();
			}
			catch (InvocationTargetException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (InterruptedException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public void invokeInGuiAndWait(final ISendOrPostCallback runnable, final Object state)
	{
		if (!isGuiInitialized() || EventQueue.isDispatchThread())
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
		else
		{
			try
			{
				EventQueue.invokeAndWait(new Runnable()
				{
					@Override
					public void run()
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
				});
			}
			catch (InvocationTargetException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (InterruptedException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public void invokeInGui(final IBackgroundWorkerDelegate runnable)
	{
		if (!isGuiInitialized() || EventQueue.isDispatchThread())
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
		else
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
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
			});
		}
	}

	@Override
	public void invokeInGui(final ISendOrPostCallback runnable, final Object state)
	{
		if (!isGuiInitialized() || EventQueue.isDispatchThread())
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
		else
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
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
			});
		}
	}

	@Override
	public void invokeInGuiLate(IBackgroundWorkerDelegate runnable)
	{
		invokeInGui(runnable);
	}

	@Override
	public void invokeInGuiLate(ISendOrPostCallback runnable, Object state)
	{
		invokeInGui(runnable, state);
	}

	@Override
	public void invokeOutOfGui(final IBackgroundWorkerDelegate runnable)
	{
		if (executor == null || !isGuiInitialized() || !EventQueue.isDispatchThread())
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
		else
		{
			executor.execute(new Runnable()
			{
				@Override
				public void run()
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
			});
		}
	}
}