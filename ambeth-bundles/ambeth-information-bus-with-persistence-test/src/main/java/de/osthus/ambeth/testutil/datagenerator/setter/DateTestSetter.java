package de.osthus.ambeth.testutil.datagenerator.setter;

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
