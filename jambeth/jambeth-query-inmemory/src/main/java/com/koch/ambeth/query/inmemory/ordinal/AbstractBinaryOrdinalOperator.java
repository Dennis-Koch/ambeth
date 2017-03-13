package com.koch.ambeth.query.inmemory.ordinal;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.inmemory.AbstractOperator;
import com.koch.ambeth.query.inmemory.IInMemoryBooleanOperand;
import com.koch.ambeth.query.inmemory.IInMemoryNumericOperand;
import com.koch.ambeth.util.collections.IMap;

public abstract class AbstractBinaryOrdinalOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Property
	protected IInMemoryNumericOperand leftOperand;

	@Property
	protected IInMemoryNumericOperand rightOperand;

	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
	{
		Double leftValue = leftOperand.evaluateNumber(nameToValueMap);
		if (leftValue == null)
		{
			return null;
		}
		Double rightValue = rightOperand.evaluateNumber(nameToValueMap);
		if (rightValue == null)
		{
			return null;
		}
		return evaluateIntern(leftValue.doubleValue(), rightValue.doubleValue());
	}

	protected abstract Boolean evaluateIntern(double leftValue, double rightValue);
}
