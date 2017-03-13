package com.koch.ambeth.query.inmemory.text;

public class EndsWithOperator extends AbstractBinaryTextOperator
{
	@Override
	protected Boolean evaluateIntern(String leftValue, String rightValue)
	{
		return Boolean.valueOf(leftValue.endsWith(rightValue));
	}
}
