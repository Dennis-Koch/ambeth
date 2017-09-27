package com.koch.ambeth.ioc.util;

/*-
 * #%L
 * jambeth-ioc
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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.Month;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.format.ISO8601DateFormat;
import com.koch.ambeth.util.format.XmlHint;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class ConversionHelper extends IConversionHelper implements IThreadLocalCleanupBean {
	protected static final boolean java7recognized =
			"1.7".equals(System.getProperty("java.specification.version"));

	protected static final HashMap<Character, Class<?>> primitiveCharToTypeMap = new HashMap<>(0.5f);

	protected static final HashMap<String, Class<?>> primitiveNameToTypeMap = new HashMap<>(0.5f);

	static {
		primitiveCharToTypeMap.put(Character.valueOf('Z'), Boolean.TYPE);
		primitiveCharToTypeMap.put(Character.valueOf('B'), Byte.TYPE);
		primitiveCharToTypeMap.put(Character.valueOf('C'), Character.TYPE);
		primitiveCharToTypeMap.put(Character.valueOf('S'), Short.TYPE);
		primitiveCharToTypeMap.put(Character.valueOf('I'), Integer.TYPE);
		primitiveCharToTypeMap.put(Character.valueOf('F'), Float.TYPE);
		primitiveCharToTypeMap.put(Character.valueOf('J'), Long.TYPE);
		primitiveCharToTypeMap.put(Character.valueOf('D'), Double.TYPE);

		primitiveNameToTypeMap.put("boolean", Boolean.TYPE);
		primitiveNameToTypeMap.put("byte", Byte.TYPE);
		primitiveNameToTypeMap.put("char", Character.TYPE);
		primitiveNameToTypeMap.put("short", Short.TYPE);
		primitiveNameToTypeMap.put("int", Integer.TYPE);
		primitiveNameToTypeMap.put("float", Float.TYPE);
		primitiveNameToTypeMap.put("long", Long.TYPE);
		primitiveNameToTypeMap.put("double", Double.TYPE);
	}

	public static final DateFormat createISO8601DateFormat() {
		if (java7recognized) {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		}
		return new ISO8601DateFormat();
	}

	protected final Method enumValueOf;

	protected final ThreadLocal<DateFormat> iso8601_DateFormatTL = new SensitiveThreadLocal<>();

	@Autowired
	protected IClassCache classCache;

	protected final HashMap<Class<?>, IDedicatedConverter> expectedTypeToDefaultMap =
			new HashMap<>(0.5f);

	public ConversionHelper() {
		try {
			enumValueOf = Enum.class.getMethod("valueOf", Class.class, String.class);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		register(BigInteger.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (BigDecimal.class.isAssignableFrom(sourceType)) {
					return ((BigDecimal) value).toBigInteger();
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return BigInteger.valueOf(((Number) value).longValue());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return new BigInteger((String) value);
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return BigInteger.valueOf((Boolean) value ? 1 : 0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(BigDecimal.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Double.class.isAssignableFrom(sourceType) || Float.class.isAssignableFrom(sourceType)) {
					return BigDecimal.valueOf(((Number) value).doubleValue()).stripTrailingZeros();
				}
				else if (BigInteger.class.isAssignableFrom(sourceType)) {
					return new BigDecimal((BigInteger) value);
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return BigDecimal.valueOf(((Number) value).longValue());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return new BigDecimal((String) value);
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return BigDecimal.valueOf((Boolean) value ? 1 : 0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Double.class, Double.TYPE, Number.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Double.class.equals(sourceType)) {
					return value;
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Double.valueOf(((Number) value).doubleValue());
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					return Double.valueOf(((Date) value).getTime());
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return Double.valueOf(((Instant) value).toEpochMilli());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return Double.valueOf((String) value);
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return (Boolean) value ? Double.valueOf(1) : Double.valueOf(0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Long.class, Long.TYPE, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Long.class.equals(sourceType)) {
					return value;
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Long.valueOf(((Number) value).longValue());
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					return Long.valueOf(((Date) value).getTime());
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return Long.valueOf(((Instant) value).toEpochMilli());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return Long.valueOf((long) Double.parseDouble((String) value));
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return (Boolean) value ? Long.valueOf(1) : Long.valueOf(0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Integer.class, Integer.TYPE, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Integer.class.equals(sourceType)) {
					return value;
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Integer.valueOf(((Number) value).intValue());
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					return Integer.valueOf((int) ((Date) value).getTime());
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return Integer.valueOf((int) ((Instant) value).toEpochMilli());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return Integer.valueOf((int) Double.parseDouble((String) value));
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return (Boolean) value ? Integer.valueOf(1) : Integer.valueOf(0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Float.class, Float.TYPE, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Float.class.equals(sourceType)) {
					return value;
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Float.valueOf(((Number) value).floatValue());
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					return Float.valueOf(((Date) value).getTime());
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return Float.valueOf(((Instant) value).toEpochMilli());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return Float.valueOf((float) Double.parseDouble((String) value));
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return (Boolean) value ? Float.valueOf(1) : Float.valueOf(0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Short.class, Short.TYPE, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Short.class.equals(sourceType)) {
					return value;
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Short.valueOf(((Number) value).shortValue());
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					return Short.valueOf((short) ((Date) value).getTime());
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return Short.valueOf((short) ((Instant) value).toEpochMilli());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return Short.valueOf((short) Double.parseDouble((String) value));
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return (Boolean) value ? Short.valueOf((short) 1) : Short.valueOf((short) 0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Byte.class, Byte.TYPE, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Byte.class.equals(sourceType)) {
					return value;
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Byte.valueOf(((Number) value).byteValue());
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					return Byte.valueOf((byte) ((Date) value).getTime());
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return Byte.valueOf((byte) ((Instant) value).toEpochMilli());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return Byte.valueOf((byte) Double.parseDouble((String) value));
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return (Boolean) value ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Boolean.class, Boolean.TYPE, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Boolean.class.equals(sourceType)) {
					return value;
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Boolean.valueOf(((Number) value).byteValue() != 0);
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					return Boolean.valueOf((byte) ((Date) value).getTime() != 0);
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return Boolean.valueOf((byte) ((Instant) value).toEpochMilli() != 0);
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					return Boolean.valueOf((String) value);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Character.class, Character.TYPE, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Character.class.equals(sourceType)) {
					return value;
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Character.valueOf((char) ((Number) value).intValue());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					String stringValue = (String) value;
					if (stringValue.length() > 0) {
						return Character.valueOf(stringValue.charAt(0));
					}
					return null;
				}
				else if (Boolean.class.isAssignableFrom(sourceType)) {
					return (Boolean) value ? Character.valueOf((char) 1) : Character.valueOf((char) 0);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Date.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (java.sql.Date.class.isAssignableFrom(sourceType)) {
					return new Date(((java.sql.Date) value).getTime());
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return new Date(((Instant) value).toEpochMilli());
				}
				else if (XMLGregorianCalendar.class.isAssignableFrom(sourceType)) {
					return ((XMLGregorianCalendar) value).toGregorianCalendar().getTime();
				}
				else if (GregorianCalendar.class.isAssignableFrom(sourceType)) {
					return ((GregorianCalendar) value).getTime();
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					try {
						return getISO_8601_DateFormat().parse((String) value);
					}
					catch (ParseException e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return new Date(((Number) value).longValue());
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Instant.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (java.util.Date.class.isAssignableFrom(sourceType)) {
					return Instant.ofEpochMilli(((java.util.Date) value).getTime());
				}
				else if (XMLGregorianCalendar.class.isAssignableFrom(sourceType)) {
					return Instant
							.ofEpochMilli(((XMLGregorianCalendar) value).toGregorianCalendar().getTimeInMillis());
				}
				else if (GregorianCalendar.class.isAssignableFrom(sourceType)) {
					return Instant.ofEpochMilli(((GregorianCalendar) value).getTimeInMillis());
				}
				else if (CharSequence.class.isAssignableFrom(sourceType)) {
					return Instant.parse((CharSequence) value);
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return Instant.ofEpochMilli(((Number) value).longValue());
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(DayOfWeek.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Number.class.isAssignableFrom(sourceType)) {
					return DayOfWeek.of(((Number) value).intValue());
				}
				else if (CharSequence.class.isAssignableFrom(sourceType)) {
					return DayOfWeek.valueOf(((CharSequence) value).toString());
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Duration.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Number.class.isAssignableFrom(sourceType)) {
					return Duration.ofMillis(((Number) value).longValue());
				}
				else if (CharSequence.class.isAssignableFrom(sourceType)) {
					return Duration.parse((CharSequence) value);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Month.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Number.class.isAssignableFrom(sourceType)) {
					return Month.of(((Number) value).intValue());
				}
				else if (CharSequence.class.isAssignableFrom(sourceType)) {
					return Month.valueOf(((CharSequence) value).toString());
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(MonthDay.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (CharSequence.class.isAssignableFrom(sourceType)) {
					return MonthDay.parse(((CharSequence) value).toString());
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Period.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (CharSequence.class.isAssignableFrom(sourceType)) {
					return Period.parse((CharSequence) value);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Year.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Number.class.isAssignableFrom(sourceType)) {
					return Year.of(((Number) value).intValue());
				}
				else if (CharSequence.class.isAssignableFrom(sourceType)) {
					return Year.parse((CharSequence) value);
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(XMLGregorianCalendar.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Date.class.isAssignableFrom(sourceType)) {
					GregorianCalendar c = new GregorianCalendar();
					c.setTime((Date) value);
					try {
						XMLGregorianCalendar xmlcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
						return xmlcal;
					}
					catch (DatatypeConfigurationException e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Calendar.class, GregorianCalendar.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (String.class.isAssignableFrom(sourceType)) {
					try {
						GregorianCalendar calendar = new GregorianCalendar();
						calendar.setTime(getISO_8601_DateFormat().parse((String) value));
						return calendar;
					}
					catch (ParseException e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					GregorianCalendar c = new GregorianCalendar();
					c.setTime((Date) value);
					return c;
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(java.sql.Date.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Date.class.isAssignableFrom(sourceType)) {
					return new java.sql.Date(((Date) value).getTime());
				}
				else if (Date.class.isAssignableFrom(sourceType)) {
					return new java.sql.Date(((Instant) value).toEpochMilli());
				}
				else if (String.class.isAssignableFrom(sourceType)) {
					Date date;
					try {
						date = getISO_8601_DateFormat().parse((String) value);
					}
					catch (ParseException e) {
						throw RuntimeExceptionUtil.mask(e);
					}
					return new java.sql.Date(date.getTime());
				}
				else if (Number.class.isAssignableFrom(sourceType)) {
					return new java.sql.Date(((Number) value).longValue());
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(java.sql.Timestamp.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Number.class.isAssignableFrom(sourceType)) {
					return new java.sql.Timestamp(((Number) value).longValue());
				}
				else if (java.util.Date.class.isAssignableFrom(sourceType)) {
					return new java.sql.Timestamp(((java.util.Date) value).getTime());
				}
				else if (Instant.class.isAssignableFrom(sourceType)) {
					return new java.sql.Timestamp(((Instant) value).toEpochMilli());
				}
				else if (java.util.Calendar.class.isAssignableFrom(sourceType)) {
					return new java.sql.Timestamp(((java.util.Calendar) value).getTimeInMillis());
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(Class.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (String.class.isAssignableFrom(sourceType)) {
					String sValue = (String) value;
					int lengthFromEnd = 0;
					for (int a = sValue.length(); a-- > 0;) {
						char oneChar = sValue.charAt(a);
						if (oneChar == '[') {
							break;
						}
						lengthFromEnd++;
						if (lengthFromEnd > 1) {
							// not a primitive array
							lengthFromEnd = -1;
							break;
						}
					}
					try {
						if (primitiveNameToTypeMap.containsKey(sValue)) {
							return primitiveNameToTypeMap.get(sValue);
						}
						return classCache.forName(sValue);
					}
					catch (Exception e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
		register(CharSequence.class, String.class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (Class.class.isAssignableFrom(sourceType)) {
					return ((Class<?>) value).getName();
				}
				if (additionalInformation instanceof XmlHint) {
					if (Date.class.isAssignableFrom(sourceType)) {
						return getISO_8601_DateFormat().format(value);
					}
				}
				if (Calendar.class.isAssignableFrom(sourceType)) {
					return getISO_8601_DateFormat().format(((Calendar) value).getTime());
				}
				return value.toString();
			}
		});
		register(char[].class, new IDedicatedConverter() {
			@Override
			public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
					Object additionalInformation) throws Exception {
				if (CharSequence.class.isAssignableFrom(sourceType)) {
					return value.toString().toCharArray();
				}
				return throwNotSupported(expectedType, sourceType, value, additionalInformation);
			}
		});
	}

	protected void register(Class<?> oneType, IDedicatedConverter converter) {
		expectedTypeToDefaultMap.put(oneType, converter);
	}

	protected void register(Class<?> oneType, Class<?> otherType, IDedicatedConverter converter) {
		expectedTypeToDefaultMap.put(oneType, converter);
		expectedTypeToDefaultMap.put(otherType, converter);
	}

	protected void register(Class<?> oneType, Class<?> otherType, Class<?> thirdType,
			IDedicatedConverter converter) {
		expectedTypeToDefaultMap.put(oneType, converter);
		expectedTypeToDefaultMap.put(otherType, converter);
		expectedTypeToDefaultMap.put(thirdType, converter);
	}

	public void setClassCache(IClassCache classCache) {
		this.classCache = classCache;
	}

	@Override
	public void cleanupThreadLocal() {
		iso8601_DateFormatTL.set(null);
	}

	protected DateFormat getISO_8601_DateFormat() {
		DateFormat dateFormat = iso8601_DateFormatTL.get();
		if (dateFormat == null) {
			dateFormat = createISO8601DateFormat();
			iso8601_DateFormatTL.set(dateFormat);
		}
		return dateFormat;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convertValueToType(Class<T> expectedType, Object value) {
		return (T) convertValueToTypeIntern(expectedType, value, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convertValueToType(Class<T> expectedType, Object value,
			Object additionalInformation) {
		return (T) convertValueToTypeIntern(expectedType, value, additionalInformation);
	}

	protected Object convertValueToTypeIntern(Class<?> expectedType, Object value,
			Object additionalInformation) {
		if (expectedType == null || value == null) {
			return value;
		}
		Class<?> sourceType = value.getClass();
		if (expectedType.isAssignableFrom(sourceType)) {
			return value;
		}
		IDedicatedConverter defaultConverter = expectedTypeToDefaultMap.get(expectedType);
		if (defaultConverter != null) {
			try {
				return defaultConverter.convertValueToType(expectedType, sourceType, value,
						additionalInformation);
			}
			catch (Error e) {
				throw e;
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		if (expectedType.isEnum()) {
			if (String.class.isAssignableFrom(sourceType)) {
				try {
					return enumValueOf.invoke(null, expectedType, value);
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			else if (Enum.class.isAssignableFrom(sourceType)) {
				try {
					return convertValueToTypeIntern(expectedType, ((Enum<?>) value).name(),
							additionalInformation);
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		return throwNotSupported(expectedType, sourceType, value, additionalInformation);
	}

	protected Object throwNotSupported(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		throw new IllegalArgumentException(
				"Cannot convert from '" + value.getClass() + "' to '" + expectedType + "'");
	}
}
