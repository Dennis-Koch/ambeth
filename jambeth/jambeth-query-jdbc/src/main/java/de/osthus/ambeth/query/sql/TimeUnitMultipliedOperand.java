package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.query.IOperand;

public class TimeUnitMultipliedOperand implements IOperand
{
	@Property
	protected IOperand timeUnit;

	@Property
	protected IOperand multiplicatedInterval;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append('(');
		timeUnit.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(")::interval * (");
		multiplicatedInterval.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(')');
	}
}