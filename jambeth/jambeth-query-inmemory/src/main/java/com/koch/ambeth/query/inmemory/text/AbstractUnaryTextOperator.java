package com.koch.ambeth.query.inmemory.text;

import com.koch.ambeth.query.inmemory.AbstractOperator;
import com.koch.ambeth.query.inmemory.IInMemoryTextOperand;
import com.koch.ambeth.util.ParamChecker;

public abstract class AbstractUnaryTextOperator extends AbstractOperator
{
	protected IInMemoryTextOperand operand;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(operand, "operand");
	}

	public void setOperand(IInMemoryTextOperand operand)
	{
		this.operand = operand;
	}
}
