package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.query.IOperand;

public class SqlIntervalOperand implements IOperand
{

	public static final String Multiply = "*";
	protected String intervalProperty;
	protected String operator;
	protected IOperand durationProperty;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append("INTERVAL '").append(intervalProperty).append("'");
		querySB.append(" ").append(operator).append(" ");
		durationProperty.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
	}

	public void setIntervalProperty(String intervalProperty)
	{
		this.intervalProperty = intervalProperty;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public void setDurationProperty(IOperand durationProperty)
	{
		this.durationProperty = durationProperty;
	}

}
