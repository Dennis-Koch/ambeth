package de.osthus.ambeth.garbageproxy;

import java.lang.reflect.Method;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.exception.BeanAlreadyDisposedException;
import de.osthus.ambeth.util.IDisposable;

public abstract class GCProxy implements IDisposable
{
	public static final Method disposeMethod;

	static
	{
		try
		{
			disposeMethod = IDisposable.class.getDeclaredMethod("dispose");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Object target;

	protected IDisposable disposable;

	public GCProxy(Object target, IDisposable disposable)
	{
		this.target = target;
		this.disposable = disposable;
	}

	@Override
	protected final void finalize() throws Throwable
	{
		dispose();
	}

	@Override
	public final void dispose()
	{
		IDisposable disposable = this.disposable;
		if (disposable != null)
		{
			disposable.dispose();
			this.disposable = null;
		}
		target = null;
	}

	protected final Object resolveTarget()
	{
		Object target = this.target;
		if (target != null)
		{
			return target;
		}
		throw new BeanAlreadyDisposedException(
				"This handle has already been disposed. This seems like a memory leak in your application if you refer to illegal handles");
	}

	@Override
	public String toString()
	{
		Object target = this.target;
		if (target != null)
		{
			return target.toString();
		}
		return super.toString();
	}
}
