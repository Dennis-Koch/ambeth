package de.osthus.ambeth.query.inmemory;

import java.util.Map;

import de.osthus.ambeth.query.IOperand;

public interface IInMemoryBooleanOperand extends IOperand
{
	Boolean evaluate(Map<Object, Object> nameToValueMap);
}
