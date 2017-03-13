package com.koch.ambeth.query.inmemory;

import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public abstract class AbstractOperand implements IInMemoryBooleanOperand
{
	@Override
	public final void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		throw new UnsupportedOperationException();
	}
}
