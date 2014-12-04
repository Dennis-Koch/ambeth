package de.osthus.ambeth.query.inmemory.bool;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;

public class IsNotNullOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Autowired
	protected IInMemoryBooleanOperand operand;

	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
	{
		Boolean value = operand.evaluate(nameToValueMap);
		return Boolean.valueOf(value != null);
	}
}
