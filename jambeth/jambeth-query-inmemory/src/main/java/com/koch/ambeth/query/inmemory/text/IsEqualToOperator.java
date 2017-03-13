package com.koch.ambeth.query.inmemory.text;

public class IsEqualToOperator extends AbstractBinaryTextOperator
{
	@Override
	protected Boolean evaluateIntern(String leftValue, String rightValue)
	{
		return Boolean.valueOf(leftValue.equals(rightValue));
	}
}
