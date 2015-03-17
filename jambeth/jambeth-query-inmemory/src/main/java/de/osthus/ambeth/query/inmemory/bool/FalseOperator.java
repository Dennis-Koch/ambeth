package de.osthus.ambeth.query.inmemory.bool;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;

public class FalseOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Override
	public Boolean evaluate(IMap<Object, Object> nameToValueMap)
	{
		return Boolean.FALSE;
	}
}
