package de.osthus.ambeth.testutil.datagenerator;

import java.util.Map;


public interface ITestSetter {

	public boolean isApplicable(Class<?> parameter);
	
	public Object createParameter(String attribute, Map<Object, Object> arguments);
	
	public void compareResult(String attribute, Map<Object, Object> arguments, Object content);
}
