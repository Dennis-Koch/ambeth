package de.osthus.ambeth.query.inmemory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
	public final void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void operate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
