package com.koch.ambeth.ioc;


public class DisposableBeanHook implements IDisposableBean
{
	private final IDisposableBean hook;

	public DisposableBeanHook(IDisposableBean hook)
	{
		this.hook = hook;
	}

	@Override
	public void destroy() throws Throwable
	{
		hook.destroy();
	}
}
