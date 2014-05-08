package de.osthus.ambeth.util;

public interface IParamHolder<T>
{
	T getValue();

	void setValue(T value);
}
