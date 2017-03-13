package com.koch.ambeth.example.conversionHelper;

import java.util.Calendar;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IConversionHelper;

public class ConversionHelperExample {
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	public void logCalendar(Calendar calendar) {
		if (log.isInfoEnabled()) {
			String stringValue = conversionHelper.convertValueToType(String.class, calendar);
			log.info(stringValue); // log ISO8601-formatted calendar value
		}
	}
}
