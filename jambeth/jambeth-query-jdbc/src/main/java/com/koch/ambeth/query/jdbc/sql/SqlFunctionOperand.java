package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SqlFunctionOperand implements IOperand, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String name;

	protected IOperand[] operands;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(name, "name");
		ParamChecker.assertNotNull(operands, "operands");
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setOperands(IOperand[] operands)
	{
		this.operands = operands;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		Object existingHint = nameToValueMap.get(QueryConstants.EXPECTED_TYPE_HINT);
		if (existingHint != null)
		{
			nameToValueMap.put(QueryConstants.EXPECTED_TYPE_HINT, null);
		}
		try
		{
			querySB.append(name).append('(');

			boolean notFirst = false;
			for (int i = 0; i < operands.length; i++)
			{
				IOperand operand = operands[i];
				if (notFirst)
				{
					querySB.append(',');
				}
				notFirst = true;
				operand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			}
			querySB.append(')');
		}
		finally
		{
			if (existingHint != null)
			{
				nameToValueMap.put(QueryConstants.EXPECTED_TYPE_HINT, existingHint);
			}
		}
	}
}