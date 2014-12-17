package de.osthus.ambeth.ioc.threadlocal;


public class ReferenceValueResolver implements IForkedValueResolver
{
	protected final Object forkedValue;

	public ReferenceValueResolver(Object forkedValue)
	{
		this.forkedValue = forkedValue;
	}

	@Override
	public Object getForkedValue()
	{
		return forkedValue;
	}
}
