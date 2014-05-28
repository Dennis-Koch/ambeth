package de.osthus.ambeth.query.inmemory.text;

import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryTextOperand;
import de.osthus.ambeth.util.ParamChecker;

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
