package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.List;
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
	protected void preProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) throws IOException
	{
		if (parameters != null)
		{
			return;
		}
		super.preProcessRightOperand(querySB, nameToValueMap, null);
		querySB.append('%');
	}

	@Override
	protected void postProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) throws IOException
	{
		if (parameters != null)
		{
			return;
		}
		querySB.append('%');
		super.postProcessRightOperand(querySB, nameToValueMap, null);
	}

	@Override
	protected void processRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Class<?> leftOperandFieldType,
			List<Object> parameters) throws IOException
	{
		nameToValueMap.put(QueryConstants.PRE_VALUE_KEY, "%");
		nameToValueMap.put(QueryConstants.POST_VALUE_KEY, "%");
		super.processRightOperand(querySB, nameToValueMap, joinQuery, leftOperandFieldType, parameters);
		nameToValueMap.remove(QueryConstants.PRE_VALUE_KEY);
		nameToValueMap.remove(QueryConstants.POST_VALUE_KEY);
	}
}
