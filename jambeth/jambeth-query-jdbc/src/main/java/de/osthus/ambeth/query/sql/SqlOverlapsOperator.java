package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;

public class SqlOverlapsOperator implements IOperator
{
	@SuppressWarnings("unused")
	@LogInstance(SqlOverlapsOperator.class)
	private ILogger log;

	protected IOperand leftOperand;
	protected IOperand leftInterval;
	protected IOperand rightOperand;
	protected IOperand rightInterval;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		this.operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	// ("TIMEENTRY"."START", INTERVAL '1 min' * "TIMEENTRY"."DURATION") OVERLAPS ('"2015-06-13 08:45:00"', INTERVAL '1 min' * 30)
	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append("(");
		leftOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(",  ");
		leftInterval.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(")");
		querySB.append(" OVERLAPS ");
		querySB.append("(");
		rightOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(",  ");
		rightInterval.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(")");
	}

	public void setLeftOperand(IOperand leftOperand)
	{
		this.leftOperand = leftOperand;
	}

	public void setLeftInterval(IOperand leftInterval)
	{
		this.leftInterval = leftInterval;
	}

	public void setRightOperand(IOperand rightOperand)
	{
		this.rightOperand = rightOperand;
	}

	public void setRightInterval(IOperand rightInterval)
	{
		this.rightInterval = rightInterval;
	}
}
