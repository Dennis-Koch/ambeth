package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Map;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlIsNotEqualToOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance(SqlIsNotEqualToOperator.class)
	private ILogger log;

	@Override
	protected void expandOperatorQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) throws IOException
	{
		if (rightValueIsNull)
		{
			querySB.append(" IS NOT ");
		}
		else
		{
			querySB.append("<>");
		}
	}
}
