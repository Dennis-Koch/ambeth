package de.osthus.ambeth.merge;

public interface IProxyHelper
{
	Class<?> getRealType(Class<?> type);

	boolean objectEquals(Object leftObject, Object rightObject);
}