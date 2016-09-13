package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.query.IOperand;

public class OverlapsOperand implements IOperand
{
	@Property
	protected IOperand leftOperand;

	@Property
	protected IOperand rightOperand;

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		querySB.append('(');
		leftOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(" OVERLAPS ");
		rightOperand.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
		querySB.append(')');
	}
}