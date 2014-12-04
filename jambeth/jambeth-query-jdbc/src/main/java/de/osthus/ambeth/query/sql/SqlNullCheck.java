package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;

public class SqlNullCheck implements IOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IOperand operand;

	@Property
	protected Boolean isNull;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
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
