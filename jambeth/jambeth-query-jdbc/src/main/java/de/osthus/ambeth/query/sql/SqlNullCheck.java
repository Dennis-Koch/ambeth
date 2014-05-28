package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Map;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.util.ParamChecker;

public class SqlNullCheck implements IOperator, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(SqlNullCheck.class)
	private ILogger log;

	protected IOperand operand;

	protected Boolean isNull;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(operand, "operand");
		ParamChecker.assertNotNull(isNull, "isNull");
	}

	public void setOperand(IOperand operand)
	{
		this.operand = operand;
	}

	public void setIsNull(boolean isNull)
	{
		this.isNull = isNull;
	}

	@Override
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		operate(querySB, nameToValueMap, joinQuery, params);
	}

	@Override
	public void operate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		operand.expandQuery(querySB, nameToValueMap, joinQuery, params);
		if (isNull)
		{
			querySB.append(" IS NULL");
		}
		else
		{
			querySB.append(" IS NOT NULL");
		}
	}
}
