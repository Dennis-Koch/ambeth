package de.osthus.ambeth.bytecode;

public interface IBaseEntity<T>
{
	String getName();

	/**
	 * "Normal" api
	 */
	void setName(String name);

	String getValue();

	/**
	 * "Fluent" api
	 */
	T setValue(String value);
}
