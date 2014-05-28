package de.osthus.ambeth.query.inmemory.bool;

import java.util.Map;

import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;
import de.osthus.ambeth.util.ParamChecker;

public class NotOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	protected IInMemoryBooleanOperand operand;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(operand, "operand");
	}

	public void setOperand(IInMemoryBooleanOperand operand)
	{
		this.operand = operand;
	}

	@Override
	public Boolean evaluate(Map<Object, Object> nameToValueMap)
	{
		Boolean value = operand.evaluate(nameToValueMap);
		if (value == null)
		{
			return null;
		}
		return Boolean.valueOf(!value.booleanValue());
	}
}
