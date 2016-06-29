package de.osthus.ambeth.query;

import java.util.Map;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
