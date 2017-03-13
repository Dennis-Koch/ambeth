package com.koch.ambeth.persistence;

public interface IColumnEntry
{
	String getFieldName();

	int getColumnIndex();

	Class<?> getJavaType();

	String getTypeName();

	boolean isNullable();

	int getRadix();

	/**
	 * Returns the information whether a mapping to a property of an entity is expected. If a mapping is not possible a warning will be logged if the mapping is
	 * expected (flag is true)
	 * 
	 * @return true if a warning should be logged when a property can not be resolved at evaluation time
	 */
	boolean expectsMapping();
}
