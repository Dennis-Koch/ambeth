package de.osthus.ambeth.query.inmemory.ordinal;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;
import de.osthus.ambeth.query.inmemory.IInMemoryNumericOperand;

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
