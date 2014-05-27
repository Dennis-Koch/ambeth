package de.osthus.ambeth.query.inmemory.text;

import java.util.Map;

import de.osthus.ambeth.query.inmemory.IInMemoryNumericOperand;

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
