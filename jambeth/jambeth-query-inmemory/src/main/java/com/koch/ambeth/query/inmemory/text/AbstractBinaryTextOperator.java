package com.koch.ambeth.query.inmemory.text;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.inmemory.AbstractOperator;
import com.koch.ambeth.query.inmemory.IInMemoryBooleanOperand;
import com.koch.ambeth.query.inmemory.IInMemoryTextOperand;
import com.koch.ambeth.util.collections.IMap;

public abstract class AbstractBinaryTextOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Property
	protected IInMemoryTextOperand leftOperand;

	@Property
	protected IInMemoryTextOperand rightOperand;

	@Override
	public final Boolean evaluate(IMap<Object, Object> nameToValueMap)
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
