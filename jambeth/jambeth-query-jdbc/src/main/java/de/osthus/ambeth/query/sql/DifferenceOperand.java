package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.util.ParamChecker;

public class DifferenceOperand implements IOperand, IInitializingBean
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
			querySB.append('-');
			operands[a].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
		if (operands.length > 1)
		{
			querySB.append(')');
		}
	}
}