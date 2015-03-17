package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.TwoPlaceOperator;

public class SqlIsLessThanOrEqualToOperator extends TwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance(SqlIsLessThanOrEqualToOperator.class)
	private ILogger log;

	@Override
	protected void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean rightValueIsNull)
	{
		querySB.append("<=");
	}
}
