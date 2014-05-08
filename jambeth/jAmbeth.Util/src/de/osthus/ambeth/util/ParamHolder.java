package de.osthus.ambeth.util;

public class ParamHolder<T> implements IParamHolder<T>
{
	protected T value;

	public ParamHolder()
	{
		// Intended blank
	}

	public ParamHolder(T value)
	{
		this.value = value;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	@Override
	public void setValue(T value)
	{
		this.value = value;
	}
}
