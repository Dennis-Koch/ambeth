package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlLikeOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void preProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) throws IOException
	{
		if (parameters != null)
		{
			// Intended blank
		}
		else
		{
			if (SqlEscapeHelper.escapeIfNecessary(this, nameToValueMap))
			{
				querySB.append('\'');
			}
		}
	}

	@Override
	protected void postProcessRightOperand(Appendable querySB, Map<Object, Object> nameToValueMap, List<Object> parameters) throws IOException
	{
		if (parameters != null)
		{
			// Intended blank
		}
		else
		{
			if (SqlEscapeHelper.unescapeIfNecessary(this, nameToValueMap))
			{
				querySB.append('\'');
			}
		}
	}

	@Override
	protected void expandOperatorQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean rightValueIsNull) throws IOException
	{
		querySB.append(" LIKE ");
	}

	@Override
	protected void postProcessOperate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
	{
		querySB.append(" ESCAPE '\\'");
		super.postProcessOperate(querySB, nameToValueMap, joinQuery, parameters);
	}
}
