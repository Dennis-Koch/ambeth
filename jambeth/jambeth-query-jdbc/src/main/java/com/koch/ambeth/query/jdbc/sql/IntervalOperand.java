package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class IntervalOperand implements IOperand
{
	@Property
	protected IOperand lowerBoundary;

	@Property
	protected IOperand upperBoundary;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append('(');
		lowerBoundary.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(',');
		upperBoundary.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(')');

	}
}