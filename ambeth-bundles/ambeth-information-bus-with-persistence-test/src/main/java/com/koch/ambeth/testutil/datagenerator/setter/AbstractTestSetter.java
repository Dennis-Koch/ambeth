package com.koch.ambeth.testutil.datagenerator.setter;

import java.util.Map;

import org.junit.Assert;

import com.koch.ambeth.testutil.datagenerator.ITestSetter;

public abstract class AbstractTestSetter implements ITestSetter
{

	private final Class<?> type;

	public AbstractTestSetter(Class<?> type)
	{
		this.type = type;
	}

	@Override
	public boolean isApplicable(Class<?> parameter)
	{
		return type.isAssignableFrom(parameter);
	}

	@Override
	public void compareResult(String propertyName, Map<Object, Object> arguments, Object content)
	{
		Object parameter = createParameter(propertyName, arguments);
		Assert.assertEquals(propertyName, parameter, content);
	}
}
