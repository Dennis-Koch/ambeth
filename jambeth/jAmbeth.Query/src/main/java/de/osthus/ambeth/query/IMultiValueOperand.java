package de.osthus.ambeth.query;

import java.util.Map;

import de.osthus.ambeth.collections.IList;

public interface IMultiValueOperand
{
	IList<Object> getMultiValue(Map<Object, Object> nameToValueMap);

	boolean isNull(Map<Object, Object> nameToValueMap);

	boolean isNullOrEmpty(Map<Object, Object> nameToValueMap);
}