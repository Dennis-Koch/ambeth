package de.osthus.ambeth.query;

import java.io.IOException;
import java.util.Map;

public interface IOperator extends IOperand
{
	void operate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException;
}
