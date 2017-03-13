package com.koch.ambeth.testutil.datagenerator.setter;

import java.util.Map;


/**
 * Sets or tests a Integer property. If a <code>String</code> argument with key <code>IntegerTestSetter.class</code> is given,
 * the String is added to the propertyName.
 * 
 * @author stefan.may
 *
 */
public class IntegerTestSetter extends AbstractTestSetter {

	public IntegerTestSetter() {
		super(int.class);
	}

	@Override
	public Object createParameter(String propertyName, Map<Object, Object> arguments) {
		if(arguments != null && arguments.containsKey(IntegerTestSetter.class)) {
			propertyName += (String) arguments.get(IntegerTestSetter.class);
		}
		
		// Some fields have only limited accuracy, %100 should be sufficient
		return propertyName.hashCode() % 100;
	}
}
