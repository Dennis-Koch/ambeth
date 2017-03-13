package com.koch.ambeth.query.inmemory;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public abstract class AbstractOperator implements IOperator, IInitializingBean
{
	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public final void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		throw new UnsupportedOperationException();
	}
}
