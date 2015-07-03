package de.osthus.ambeth.pg;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.query.IOperand;

public class PgSqlRegexpLikeOperand implements IOperand
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Property
	protected IOperand sourceString;

	@Property
	protected IOperand pattern;

	@Property
	protected IOperand matchParameter;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append('(');
		sourceString.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(" ~");
		if (matchParameter != null)
		{
			matchParameter.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		}
		querySB.append(' ');
		pattern.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(')');
	}
}