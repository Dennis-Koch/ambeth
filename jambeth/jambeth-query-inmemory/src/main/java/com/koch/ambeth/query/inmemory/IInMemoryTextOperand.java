package com.koch.ambeth.query.inmemory;

import java.util.Map;

import com.koch.ambeth.query.IOperand;

public interface IInMemoryTextOperand extends IOperand
{
	String evaluateText(Map<Object, Object> nameToValueMap);
}
