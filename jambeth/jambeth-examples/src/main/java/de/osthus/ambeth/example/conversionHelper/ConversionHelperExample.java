package de.osthus.ambeth.example.conversionHelper;

import java.util.Calendar;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IConversionHelper;

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
