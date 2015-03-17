package de.osthus.ambeth.query.inmemory;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.query.IOperand;

public interface IInMemoryBooleanOperand extends IOperand
{
	Boolean evaluate(IMap<Object, Object> nameToValueMap);
}
