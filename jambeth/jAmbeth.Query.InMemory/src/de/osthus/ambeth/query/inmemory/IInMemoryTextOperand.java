package de.osthus.ambeth.query.inmemory;

import java.util.Map;

import de.osthus.ambeth.query.IOperand;

public interface IInMemoryTextOperand extends IOperand
{
	String evaluateText(Map<Object, Object> nameToValueMap);
}
