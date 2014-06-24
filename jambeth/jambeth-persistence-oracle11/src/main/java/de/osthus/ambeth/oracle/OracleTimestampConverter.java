package de.osthus.ambeth.oracle;

import java.util.Calendar;
import java.util.Date;

import oracle.sql.TIMESTAMP;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IDedicatedConverter;

public class OracleTimestampConverter implements IInitializingBean, IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Calendar vmCalendar = Calendar.getInstance();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (sourceType.equals(TIMESTAMP.class))
		{
			long longValue;
			try
			{
				longValue = ((TIMESTAMP) value).timestampValue(vmCalendar).getTime();
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			if (Long.class.equals(expectedType) || Long.TYPE.equals(expectedType))
			{
				return Long.valueOf(longValue);
			}
			else if (Date.class.equals(expectedType))
			{
				return new Date(longValue);
			}
			else if (Calendar.class.equals(expectedType))
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(longValue);
				// skip cloning the TimeZone with e.g.: calendar.setTimeZone(vmCalendar.getTimeZone());
				// the timezone is already correct because both calendar instances contain the "default" timezone of the vm
				return calendar;
			}
		}
		throw new IllegalStateException("Conversion " + sourceType.getName() + "->" + expectedType.getName() + " not supported");
	}
}
