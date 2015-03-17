package de.osthus.ambeth.query.inmemory.bool;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;

public class AndOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Property
	protected IInMemoryBooleanOperand[] operands;

	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
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
