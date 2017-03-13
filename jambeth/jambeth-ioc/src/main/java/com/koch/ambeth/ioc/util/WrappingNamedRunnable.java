package com.koch.ambeth.ioc.util;


public class WrappingNamedRunnable implements INamedRunnable
{
	protected final Runnable runnable;

	protected final String name;

	public WrappingNamedRunnable(Runnable runnable, String name)
	{
		this.runnable = runnable;
		this.name = name;
	}

	@Override
	public void run()
	{
		runnable.run();
	}

	@Override
	public String getName()
	{
		return name;
	}
}
