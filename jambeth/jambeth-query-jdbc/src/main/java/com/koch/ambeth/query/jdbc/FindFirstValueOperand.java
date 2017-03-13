package com.koch.ambeth.query.jdbc;

import java.util.Map;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class FindFirstValueOperand implements IValueOperand, IOperand
{
	@LogInstance
	private ILogger log;

	protected IValueOperand[] operands;

	@Override
	public Object getValue(Map<Object, Object> nameToValueMap)
	{
		if (operands == null)
		{
			return null;
		}
		for (int i = 0; i < operands.length; i++)
		{
			IValueOperand operand = operands[i];
			if (operand != null)
			{
				Object value = operand.getValue(nameToValueMap);
				if (value != null)
				{
					return value;
				}
			}
		}
		return null;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		throw new IllegalStateException("expandQuery not implemented yet for FindFirstValueOperand");
	}
}
