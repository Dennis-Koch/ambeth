package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SqlContainsOperator extends SqlLikeOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (parameters != null)
		{
			return;
		}
		super.preProcessRightOperand(querySB, nameToValueMap, null);
		querySB.append('%');
	}

	@Override
	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (parameters != null)
		{
			return;
		}
		querySB.append('%');
		super.postProcessRightOperand(querySB, nameToValueMap, null);
	}

	@Override
	protected void processRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType,
			IList<Object> parameters)
	{
		nameToValueMap.put(QueryConstants.PRE_VALUE_KEY, "%");
		nameToValueMap.put(QueryConstants.POST_VALUE_KEY, "%");
		super.processRightOperand(querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
		nameToValueMap.remove(QueryConstants.PRE_VALUE_KEY);
		nameToValueMap.remove(QueryConstants.POST_VALUE_KEY);
	}
}
