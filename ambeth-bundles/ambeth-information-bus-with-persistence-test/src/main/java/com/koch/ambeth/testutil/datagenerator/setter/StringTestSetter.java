package com.koch.ambeth.testutil.datagenerator.setter;

import java.util.Map;


/**
 * Sets or tests a String property. If a <code>String</code> argument with key <code>StringTestSetter.class</code> is given,
 * the String is added to the propertyName.
 * 
 * @author stefan.may
 *
 */
public class StringTestSetter extends AbstractTestSetter {

	public StringTestSetter() {
		super(String.class);
	}

	@Override
	public Object createParameter(String propertyName, Map<Object, Object> arguments) {
		if(arguments != null && arguments.containsKey(StringTestSetter.class)) {
			propertyName += (String) arguments.get(StringTestSetter.class);
		}
		
		return propertyName;
	}
}
