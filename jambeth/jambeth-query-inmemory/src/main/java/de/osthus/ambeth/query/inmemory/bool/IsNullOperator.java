package de.osthus.ambeth.query.inmemory.bool;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;

public class IsNullOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Property
	protected IInMemoryBooleanOperand operand;

	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
	{
		Boolean value = operand.evaluate(nameToValueMap);
		return Boolean.valueOf(value == null);
	}
}
