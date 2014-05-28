package de.osthus.ambeth.query.inmemory.text;

import java.util.Map;

import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;
import de.osthus.ambeth.query.inmemory.IInMemoryTextOperand;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractBinaryTextOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	protected IInMemoryTextOperand leftOperand;

	protected IInMemoryTextOperand rightOperand;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(leftOperand, "leftOperand");
		ParamChecker.assertNotNull(rightOperand, "rightOperand");
	}

	public void setLeftOperand(IInMemoryTextOperand leftOperand)
	{
		this.leftOperand = leftOperand;
	}

	public void setRightOperand(IInMemoryTextOperand rightOperand)
	{
		this.rightOperand = rightOperand;
	}

	@Override
	public final Boolean evaluate(Map<Object, Object> nameToValueMap)
	{
		String leftValue = leftOperand.evaluateText(nameToValueMap);
		if (leftValue == null)
		{
			return null;
		}
		String rightValue = rightOperand.evaluateText(nameToValueMap);
		if (rightValue == null)
		{
			return null;
		}
		return evaluateIntern(leftValue, rightValue);
	}

	protected abstract Boolean evaluateIntern(String leftValue, String rightValue);
}
