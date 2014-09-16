package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.util.ParamChecker;

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
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
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
}