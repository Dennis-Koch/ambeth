package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.jdbc.TwoPlaceOperator;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IMap;

public class SqlIsLessThanOperator extends TwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance(SqlIsLessThanOperator.class)
	private ILogger log;

	@Override
	protected void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean rightValueIsNull)
	{
		querySB.append("<");
	}
}
