package de.osthus.ambeth.testutil.datagenerator;

import java.util.Map;

public interface ITestSetter
{

	boolean isApplicable(Class<?> parameter);

	Object createParameter(String attribute, Map<Object, Object> arguments);

	void compareResult(String attribute, Map<Object, Object> arguments, Object content);
}
