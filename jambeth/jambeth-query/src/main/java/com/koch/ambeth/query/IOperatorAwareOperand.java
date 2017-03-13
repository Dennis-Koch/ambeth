package com.koch.ambeth.query;

import java.util.Map;

public interface IOperatorAwareOperand
{
	void operatorStart(Map<Object, Object> nameToValueMap);

	void operatorEnd(Map<Object, Object> nameToValueMap);
}