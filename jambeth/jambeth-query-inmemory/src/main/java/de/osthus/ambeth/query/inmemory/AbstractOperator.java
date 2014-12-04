package de.osthus.ambeth.query.inmemory;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.query.IOperator;

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
