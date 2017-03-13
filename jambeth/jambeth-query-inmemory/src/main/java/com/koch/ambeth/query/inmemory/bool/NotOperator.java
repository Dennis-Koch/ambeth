package com.koch.ambeth.query.inmemory.bool;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.inmemory.AbstractOperator;
import com.koch.ambeth.query.inmemory.IInMemoryBooleanOperand;
import com.koch.ambeth.util.collections.IMap;

public class NotOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Property
	protected IInMemoryBooleanOperand operand;

	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
	{
		Boolean value = operand.evaluate(nameToValueMap);
		if (value == null)
		{
			return null;
		}
		return Boolean.valueOf(!value.booleanValue());
	}
}
