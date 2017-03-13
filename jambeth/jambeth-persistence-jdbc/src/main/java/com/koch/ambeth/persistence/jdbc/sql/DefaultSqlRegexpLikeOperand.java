package com.koch.ambeth.persistence.jdbc.sql;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class DefaultSqlRegexpLikeOperand implements IOperand
{
	@LogInstance
	private ILogger log;

	@Property
	protected IOperand sourceString;

	@Property
	protected IOperand pattern;

	@Property
	protected IOperand matchParameter;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append("REGEXP_LIKE").append('(');
		sourceString.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(',');
		pattern.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		if (matchParameter != null)
		{
			querySB.append(',');
			matchParameter.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
		querySB.append(')');
	}
}