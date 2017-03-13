package com.koch.ambeth.query.inmemory.bool;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.inmemory.AbstractOperator;
import com.koch.ambeth.query.inmemory.IInMemoryBooleanOperand;
import com.koch.ambeth.util.collections.IMap;

public class OrOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Property
	protected IInMemoryBooleanOperand[] operands;

	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
	{
		for (IInMemoryBooleanOperand operand : operands)
		{
			Boolean value = operand.evaluate(nameToValueMap);
			if (value != null && value.booleanValue())
			{
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
}
