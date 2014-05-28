package de.osthus.ambeth.query.inmemory.text;

public class IsNotEqualToOperator extends AbstractBinaryTextOperator
{
	@Override
	protected Boolean evaluateIntern(String leftValue, String rightValue)
	{
		return Boolean.valueOf(!leftValue.equals(rightValue));
	}
}
