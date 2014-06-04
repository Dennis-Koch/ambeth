package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Map;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.TwoPlaceOperator;

public class SqlIsGreaterThanOrEqualToOperator extends TwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance(SqlIsGreaterThanOrEqualToOperator.class)
	private ILogger log;

	@Override
	protected void expandOperatorQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) throws IOException
	{
		querySB.append(">=");
	}
}