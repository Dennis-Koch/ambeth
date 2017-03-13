package com.koch.ambeth.filter;

import java.util.Map;

import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class OperandDummy implements IOperator
{
	private String type;

	private Map<String, ?> attributes;

	private IOperand[] operands;

	OperandDummy(String type, IOperand... operands)
	{
		this.type = type;
		this.operands = operands;
	}

	OperandDummy(String type, Map<String, ?> attributes)
	{
		this.type = type;
		this.attributes = attributes;
	}

	OperandDummy(String type, Map<String, ?> attributes, IOperand... operands)
	{
		this.type = type;
		this.attributes = attributes;
		this.operands = operands;
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		throw new IllegalStateException("Not implemented");
	}

	public String getType()
	{
		return type;
	}

	public Map<String, ?> getAttributes()
	{
		return attributes;
	}

	public IOperand[] getOperands()
	{
		return operands;
	}
}