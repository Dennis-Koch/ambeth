package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Map;

import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlContainsOperator extends SqlLikeOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void preProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params) throws IOException
	{
		if (params != null)
		{
			return;
		}
		super.preProcessRightOperand(querySB, nameToValueMap, null);
		querySB.append('%');
	}

	@Override
	protected void postProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, Map<Integer, Object> params) throws IOException
	{
		if (params != null)
		{
			return;
		}
		querySB.append('%');
		super.postProcessRightOperand(querySB, nameToValueMap, null);
	}

	@Override
	protected void processRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType,
			Map<Integer, Object> params) throws IOException
	{
		nameToValueMap.put(QueryConstants.PRE_VALUE_KEY, "%");
		nameToValueMap.put(QueryConstants.POST_VALUE_KEY, "%");
		super.processRightOperand(querySB, nameToValueMap, joinQuery, leftOperandFieldType, params);
		nameToValueMap.remove(QueryConstants.PRE_VALUE_KEY);
		nameToValueMap.remove(QueryConstants.POST_VALUE_KEY);
	}
}
