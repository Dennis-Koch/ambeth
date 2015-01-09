package de.osthus.ambeth.ioc.threadlocal;

public class ReferenceValueResolver implements IForkedValueResolver
{
	private Object forkedValue;

	private final Object originalValue;

	public ReferenceValueResolver(Object originalValue, Object forkedValue)
	{
		this.originalValue = originalValue;
		this.forkedValue = forkedValue;
	}

	@Override
	public Object getOriginalValue()
	{
		return originalValue;
	}

	@Override
	public Object createForkedValue()
	{
		return forkedValue;
	}
}
