package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlEndsWithOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (parameters != null)
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
	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (parameters != null)
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
	protected void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean rightValueIsNull)
	{
		querySB.append(" LIKE ");
	}
}
