package de.osthus.ambeth.query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IOperand
{
	void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException;
}
