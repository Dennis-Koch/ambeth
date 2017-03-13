package com.koch.ambeth.testutil.datagenerator.setter;

import java.util.Map;


/**
 * Sets or tests a String property. If a <code>String</code> argument with key <code>StringTestSetter.class</code> is given,
 * the String is added to the propertyName.
 * 
 * @author stefan.may
 *
 */
public class BooleanTestSetter extends AbstractTestSetter {

	public BooleanTestSetter() {
		super(boolean.class);
	}

	@Override
	public Object createParameter(String propertyName, Map<Object, Object> arguments) {
		if(arguments != null && arguments.containsKey(BooleanTestSetter.class)) {
			return arguments.get(BooleanTestSetter.class);
		}
		
		return true;
	}
}
