package com.koch.ambeth.persistence.oracle;

/*-
 * #%L
 * jambeth-persistence-oracle11
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

import java.util.Calendar;
import java.util.Date;

import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import oracle.sql.TIMESTAMP;

public class OracleTimestampConverter implements IDedicatedConverter, IThreadLocalCleanupBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final ThreadLocal<Calendar> vmCalendarTL = new ThreadLocal<>();

	@Override
	public void cleanupThreadLocal() {
		vmCalendarTL.remove();
	}

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		if (sourceType.equals(TIMESTAMP.class)) {
			long longValue;
			try {
				Calendar calendar = vmCalendarTL.get();
				if (calendar == null) {
					calendar = Calendar.getInstance();
					vmCalendarTL.set(calendar);
				}
				longValue = ((TIMESTAMP) value).timestampValue(calendar).getTime();
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			if (Long.class.equals(expectedType) || Long.TYPE.equals(expectedType)) {
				return Long.valueOf(longValue);
			}
			else if (Date.class.equals(expectedType)) {
				return new Date(longValue);
			}
			else if (Calendar.class.equals(expectedType)) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(longValue);
				// skip cloning the TimeZone with e.g.: calendar.setTimeZone(vmCalendar.getTimeZone());
				// the timezone is already correct because both calendar instances contain the "default"
				// timezone of the vm
				return calendar;
			}
		}
		throw new IllegalStateException(
				"Conversion " + sourceType.getName() + "->" + expectedType.getName() + " not supported");
	}
}
