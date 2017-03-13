package com.koch.ambeth.query.inmemory.text;

import java.util.Map;

import com.koch.ambeth.query.inmemory.IInMemoryNumericOperand;

public class LengthOperator extends AbstractUnaryTextOperator implements IInMemoryNumericOperand
{
	@Override
	public Double evaluateNumber(Map<Object, Object> nameToValueMap)
	{
		String value = operand.evaluateText(nameToValueMap);
		if (value == null)
		{
			return null;
		}
		return Double.valueOf(value.length());
	}
}
