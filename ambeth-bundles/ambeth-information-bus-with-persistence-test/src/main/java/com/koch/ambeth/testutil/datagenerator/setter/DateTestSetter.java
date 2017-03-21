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

import java.util.Date;
import java.util.Map;


/**
 * Sets or tests a Date property. If a <code>String</code> argument with key <code>StringTestSetter.class</code> is
 * given, the String is added to the propertyName.
 * 
 * @author stefan.may
 * 
 */
public class DateTestSetter extends AbstractTestSetter {
	
	private static Date compareDate = new Date();

	public DateTestSetter() {
		super(Date.class);
	}

	@Override
	public Object createParameter(String propertyName, Map<Object, Object> arguments) {
		if(arguments != null && arguments.containsKey(DateTestSetter.class)) {
			propertyName += (String) arguments.get(DateTestSetter.class);
		}
		
		//Some random, but reproducable Date
		long hashLong = hashLong(propertyName);
		Date date = new Date(compareDate.getTime());
		date.setTime(date.getTime() + Math.abs(hashLong % (365*24*60*60*1000L)));
		return date;
	}

	private static long hashLong(String string) {
		long h = 1125899906842597L; // prime
		int len = string.length();

		for (int i = 0; i < len; i++) {
			h = 31 * h + string.charAt(i);
		}
		return h;
	}
}
