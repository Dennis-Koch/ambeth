package com.koch.ambeth.query;

import java.util.Map;

public interface IValueOperand
{
	Object getValue(Map<Object, Object> nameToValueMap);
}