package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SqlIsNotInOperator extends CaseSensitiveTwoPlaceOperator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void preProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		querySB.append('(');
	}

	@Override
	protected void postProcessRightOperand(IAppendable querySB, IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		querySB.append(')');
	}

	@Override
	protected boolean supportsMultiValueOperand()
	{
		return true;
	}

	@Override
	protected void expandOperatorQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean rightValueIsNull)
	{
		querySB.append(" NOT IN ");
	}
}
