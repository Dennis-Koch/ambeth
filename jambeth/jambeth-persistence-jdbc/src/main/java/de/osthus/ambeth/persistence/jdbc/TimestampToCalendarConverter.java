package de.osthus.ambeth.persistence.jdbc;

import java.util.Calendar;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IDedicatedConverter;

public class TimestampToCalendarConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(((Long) value).longValue());
		return calendar;
	}
}
