package de.osthus.ambeth.query.inmemory;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;

public abstract class AbstractOperand implements IInMemoryBooleanOperand
{
	@Override
	public final void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		throw new UnsupportedOperationException();
	}
}
