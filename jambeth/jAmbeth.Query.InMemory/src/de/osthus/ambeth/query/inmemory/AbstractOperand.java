package de.osthus.ambeth.query.inmemory;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractOperand implements IInMemoryBooleanOperand
{
	@Override
	public final void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
