package de.osthus.ambeth.query.inmemory.ordinal;

import java.util.Map;

import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;
import de.osthus.ambeth.query.inmemory.IInMemoryNumericOperand;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractBinaryOrdinalOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	protected IInMemoryNumericOperand leftOperand;

	protected IInMemoryNumericOperand rightOperand;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(leftOperand, "leftOperand");
		ParamChecker.assertNotNull(rightOperand, "rightOperand");
	}

	public void setLeftOperand(IInMemoryNumericOperand leftOperand)
	{
		this.leftOperand = leftOperand;
	}

	public void setRightOperand(IInMemoryNumericOperand rightOperand)
	{
		this.rightOperand = rightOperand;
	}

	@Override
	public Boolean evaluate(Map<Object, Object> nameToValueMap)
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
