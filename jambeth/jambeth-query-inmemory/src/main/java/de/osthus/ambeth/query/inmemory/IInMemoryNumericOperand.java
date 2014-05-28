package de.osthus.ambeth.query.inmemory;

import java.util.Map;

import de.osthus.ambeth.query.IOperand;

public interface IInMemoryNumericOperand extends IOperand
{
	Double evaluateNumber(Map<Object, Object> nameToValueMap);
}
