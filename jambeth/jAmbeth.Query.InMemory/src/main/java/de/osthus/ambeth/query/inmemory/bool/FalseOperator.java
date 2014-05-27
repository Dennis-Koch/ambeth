package de.osthus.ambeth.query.inmemory.bool;

import java.util.Map;

import de.osthus.ambeth.query.inmemory.AbstractOperator;
import de.osthus.ambeth.query.inmemory.IInMemoryBooleanOperand;

public class FalseOperator extends AbstractOperator implements IInMemoryBooleanOperand
{
	@Override
	public Boolean evaluate(Map<Object, Object> nameToValueMap)
	{
		return Boolean.FALSE;
	}
}
