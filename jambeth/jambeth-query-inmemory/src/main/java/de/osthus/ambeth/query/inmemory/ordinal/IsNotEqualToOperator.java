package de.osthus.ambeth.query.inmemory.ordinal;

public class IsNotEqualToOperator extends AbstractBinaryOrdinalOperator
{
	@Override
	protected Boolean evaluateIntern(double leftValue, double rightValue)
	{
		return Boolean.valueOf(leftValue != rightValue);
	}
}