package com.koch.ambeth.query.inmemory.bool;

import com.koch.ambeth.query.inmemory.AbstractOperator;
import com.koch.ambeth.query.inmemory.IInMemoryBooleanOperand;
import com.koch.ambeth.util.collections.IMap;

public class TrueOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
	{
		return Boolean.TRUE;
	}
}
