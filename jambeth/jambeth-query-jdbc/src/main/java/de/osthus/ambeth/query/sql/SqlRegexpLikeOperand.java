package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.util.ParamChecker;

public class SqlRegexpLikeOperand implements IOperand, IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected IOperand sourceString;
	protected IOperand pattern;
	protected IOperand matchParameter;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(sourceString, "sourceString");
		ParamChecker.assertNotNull(pattern, "pattern");
	}

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