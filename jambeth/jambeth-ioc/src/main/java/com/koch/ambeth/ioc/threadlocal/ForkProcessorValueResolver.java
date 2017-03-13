package com.koch.ambeth.ioc.threadlocal;

public class ForkProcessorValueResolver implements IForkedValueResolver
{
	private final Object originalValue;

	private final IForkProcessor forkProcessor;

	public ForkProcessorValueResolver(Object originalValue, IForkProcessor forkProcessor)
	{
		this.originalValue = originalValue;
		this.forkProcessor = forkProcessor;
	}

	public IForkProcessor getForkProcessor()
	{
		return forkProcessor;
	}

	@Override
	public Object getOriginalValue()
	{
		return originalValue;
	}

	@Override
	public Object createForkedValue()
	{
		return forkProcessor.createForkedValue(originalValue);
	}
}
