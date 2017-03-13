package com.koch.ambeth.query.inmemory.ordinal;

public class IsLessThanOrEqualToOperator extends AbstractBinaryOrdinalOperator
{
	@Override
	protected Boolean evaluateIntern(double leftValue, double rightValue)
	{
		return Boolean.valueOf(leftValue <= rightValue);
	}
}
