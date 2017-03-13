package com.koch.ambeth.query.inmemory;

import java.util.Map;

import com.koch.ambeth.query.IOperand;

public interface IInMemoryNumericOperand extends IOperand
{
	Double evaluateNumber(Map<Object, Object> nameToValueMap);
}
