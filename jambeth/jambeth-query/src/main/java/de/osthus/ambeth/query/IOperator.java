package de.osthus.ambeth.query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IOperator extends IOperand
{
	void operate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException;
}
