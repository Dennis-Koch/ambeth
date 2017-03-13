package com.koch.ambeth.query;

import java.util.Map;

import com.koch.ambeth.util.collections.IList;

public interface IMultiValueOperand
{
	IList<Object> getMultiValue(Map<Object, Object> nameToValueMap);

	boolean isNull(Map<Object, Object> nameToValueMap);

	boolean isNullOrEmpty(Map<Object, Object> nameToValueMap);
}