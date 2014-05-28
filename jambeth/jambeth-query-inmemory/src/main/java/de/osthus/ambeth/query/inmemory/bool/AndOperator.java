package de.osthus.ambeth.query.inmemory.bool;

import java.util.Map;

import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;
import de.osthus.ambeth.util.ParamChecker;

public class AndOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	protected IInMemoryBooleanOperand[] operands;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(operands, "operands");
	}

	public void setOperands(IInMemoryBooleanOperand[] operands)
	{
		this.operands = operands;
	}

	@Override
	public Boolean evaluate(Map<Object, Object> nameToValueMap)
	{
		for (IInMemoryBooleanOperand operand : operands)
		{
			Boolean value = operand.evaluate(nameToValueMap);
			if (value == null)
			{
				return null;
			}
			else if (!value.booleanValue())
			{
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}
}
