package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Map;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.util.ParamChecker;

public class SqlFunctionOperand implements IOperand, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(SqlFunctionOperand.class)
	private ILogger log;

	protected String name;

	protected IOperand[] parameters;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(name, "name");
		ParamChecker.assertNotNull(parameters, "parameters");
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setParameters(IOperand[] parameters)
	{
		this.parameters = parameters;
	}

	@Override
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		querySB.append(name).append('(');
		boolean notFirst = false;
		for (int i = 0; i < parameters.length; i++)
		{
			IOperand parameter = parameters[i];
			if (notFirst)
			{
				querySB.append(',');
			}
			notFirst = true;
			parameter.expandQuery(querySB, nameToValueMap, joinQuery, params);
		}
		querySB.append(')');
	}
}