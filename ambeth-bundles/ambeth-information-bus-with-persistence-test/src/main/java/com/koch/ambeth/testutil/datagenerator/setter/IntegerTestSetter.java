package com.koch.ambeth.testutil.datagenerator.setter;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.Map;


/**
 * Sets or tests a Integer property. If a <code>String</code> argument with key
 * <code>IntegerTestSetter.class</code> is given, the String is added to the propertyName.
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
		if (arguments != null && arguments.containsKey(IntegerTestSetter.class)) {
			propertyName += (String) arguments.get(IntegerTestSetter.class);
		}

		// Some fields have only limited accuracy, %100 should be sufficient
		return propertyName.hashCode() % 100;
	}
}
