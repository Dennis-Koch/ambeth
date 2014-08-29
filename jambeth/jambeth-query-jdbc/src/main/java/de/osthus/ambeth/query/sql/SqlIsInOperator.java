package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlIsInOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void operate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
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
	protected void expandOperatorQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) throws IOException
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
	protected void preProcessOperate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
	{
		Class<?> leftOperandFieldType = getLeftOperandFieldType();
		if (java.sql.Array.class.isAssignableFrom(leftOperandFieldType))
		{
			querySB.append("EXISTS");
		}
		super.preProcessOperate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	protected void preProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) throws IOException
	{
		querySB.append('(');
	}

	@Override
	protected void postProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) throws IOException
	{
		querySB.append(')');
	}
}
