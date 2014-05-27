package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Map;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlIsEqualToOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance(SqlIsEqualToOperator.class)
	private ILogger log;

	@Override
	protected void expandOperatorQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) throws IOException
	{
		if (rightValueIsNull)
		{
			querySB.append(" IS ");
		}
		else
		{
			querySB.append("=");
		}
	}
}
