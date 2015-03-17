package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlIsInOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		if (isRightValueNullOrEmpty(nameToValueMap))
		{
			// No-op
			querySB.append("0=1");
			return;
		}
		super.operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	protected boolean supportsMultiValueOperand()
	{
		return true;
	}

	@Override
	protected void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean rightValueIsNull)
	{
		Class<?> leftOperandFieldType = getLeftOperandFieldType();
		if (!java.sql.Array.class.isAssignableFrom(leftOperandFieldType))
		{
			querySB.append(" IN ");
			return;
		}
		querySB.append(" INTERSECT ");
	}

	@Override
	protected void preProcessOperate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		Class<?> leftOperandFieldType = getLeftOperandFieldType();
		if (java.sql.Array.class.isAssignableFrom(leftOperandFieldType))
		{
			querySB.append("EXISTS");
		}
		super.preProcessOperate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		querySB.append('(');
	}

	@Override
	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		querySB.append(')');
	}
}
