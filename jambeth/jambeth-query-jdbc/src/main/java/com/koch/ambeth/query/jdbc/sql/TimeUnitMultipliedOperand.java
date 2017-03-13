package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

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