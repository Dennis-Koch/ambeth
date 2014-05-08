package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Map;

import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlEndsWithOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void preProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params) throws IOException
	{
		if (params != null)
		{
			nameToValueMap.put(QueryConstants.PRE_VALUE_KEY, "%");
		}
		else
		{
			if (SqlEscapeHelper.escapeIfNecessary(this, nameToValueMap))
			{
				querySB.append('\'');
			}
			querySB.append('%');
		}
	}

	@Override
	protected void postProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params) throws IOException
	{
		if (params != null)
		{
			nameToValueMap.remove(QueryConstants.PRE_VALUE_KEY);
		}
		else
		{
			if (SqlEscapeHelper.unescapeIfNecessary(this, nameToValueMap))
			{
				querySB.append('\'');
			}
		}
	}

	@Override
	protected void expandOperatorQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) throws IOException
	{
		querySB.append(" LIKE ");
	}
}
