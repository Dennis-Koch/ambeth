package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.query.IOperand;

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