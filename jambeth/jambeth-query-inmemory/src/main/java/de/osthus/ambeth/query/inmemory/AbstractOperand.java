package de.osthus.ambeth.query.inmemory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractOperand implements IInMemoryBooleanOperand
{
	@Override
	public final void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
