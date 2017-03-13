package com.koch.ambeth.query.inmemory;

import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.collections.IMap;

public interface IInMemoryBooleanOperand extends IOperand
{
	Boolean evaluate(IMap<Object, Object> nameToValueMap);
}
