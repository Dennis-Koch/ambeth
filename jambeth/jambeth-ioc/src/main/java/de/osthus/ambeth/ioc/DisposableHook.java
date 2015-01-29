package de.osthus.ambeth.ioc;

import de.osthus.ambeth.util.IDisposable;

public class DisposableHook implements IDisposableBean
{
	private final IDisposable hook;

	public DisposableHook(IDisposable hook)
	{
		this.hook = hook;
	}

	@Override
	public void destroy() throws Throwable
	{
		hook.dispose();
	}
}
