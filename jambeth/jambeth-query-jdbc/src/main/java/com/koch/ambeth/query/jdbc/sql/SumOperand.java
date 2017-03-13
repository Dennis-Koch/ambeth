package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SumOperand implements IOperand, IInitializingBean
{
	@Property
	protected IOperand[] operands;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertTrue(operands.length != 0, "Operands");
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		if (operands.length > 1)
		{
			querySB.append('(');
		}
		operands[0].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		for (int a = 1, size = operands.length; a < size; a++)
		{
			querySB.append('+');
			operands[a].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
		if (operands.length > 1)
		{
			querySB.append(')');
		}
	}
}