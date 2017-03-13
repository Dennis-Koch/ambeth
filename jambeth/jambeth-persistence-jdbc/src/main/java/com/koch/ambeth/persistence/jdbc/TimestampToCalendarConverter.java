package com.koch.ambeth.persistence.jdbc;

import java.util.Calendar;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IDedicatedConverter;

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
