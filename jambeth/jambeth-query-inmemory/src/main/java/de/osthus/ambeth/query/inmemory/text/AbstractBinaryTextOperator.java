package de.osthus.ambeth.query.inmemory.text;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;
import de.osthus.ambeth.query.inmemory.IInMemoryTextOperand;

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
