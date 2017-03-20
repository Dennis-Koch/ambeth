package com.koch.ambeth.ioc.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IConversionHelper;
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
	protected IClassLoaderProvider classLoaderProvider;

	public ConversionHelper() {
		try {
			enumValueOf = Enum.class.getMethod("valueOf", Class.class, String.class);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public void setClassLoaderProvider(IClassLoaderProvider classLoaderProvider) {
		this.classLoaderProvider = classLoaderProvider;
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
		Class<?> type = value.getClass();
		if (expectedType.isAssignableFrom(type)) {
			return value;
		}
		if (BigInteger.class.equals(expectedType)) {
			if (BigDecimal.class.isAssignableFrom(type)) {
				return ((BigDecimal) value).toBigInteger();
			}
			else if (Number.class.isAssignableFrom(type)) {
				return BigInteger.valueOf(((Number) value).longValue());
			}
			else if (String.class.isAssignableFrom(type)) {
				return new BigInteger((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return BigInteger.valueOf((Boolean) value ? 1 : 0);
			}
		}
		else if (Long.class.equals(expectedType) || Long.TYPE.equals(expectedType)) {
			if (Long.class.equals(type)) {
				return value;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return Long.valueOf(((Number) value).longValue());
			}
			else if (Date.class.isAssignableFrom(type)) {
				return Long.valueOf(((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type)) {
				return Long.valueOf((long) Double.parseDouble((String) value));
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return (Boolean) value ? Long.valueOf(1) : Long.valueOf(0);
			}
		}
		else if (BigDecimal.class.equals(expectedType)) {
			if (Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type)) {
				return BigDecimal.valueOf(((Number) value).doubleValue()).stripTrailingZeros();
			}
			else if (BigInteger.class.isAssignableFrom(type)) {
				return new BigDecimal((BigInteger) value);
			}
			else if (Number.class.isAssignableFrom(type)) {
				return BigDecimal.valueOf(((Number) value).longValue());
			}
			else if (String.class.isAssignableFrom(type)) {
				return new BigDecimal((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return BigDecimal.valueOf((Boolean) value ? 1 : 0);
			}
		}
		else if (Double.class.equals(expectedType) || Double.TYPE.equals(expectedType)
				|| Number.class.equals(expectedType)) {
			if (Double.class.equals(type)) {
				return value;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return Double.valueOf(((Number) value).doubleValue());
			}
			else if (Date.class.isAssignableFrom(type)) {
				return Double.valueOf(((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type)) {
				return Double.valueOf((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return (Boolean) value ? Double.valueOf(1) : Double.valueOf(0);
			}
		}
		else if (Integer.class.equals(expectedType) || Integer.TYPE.equals(expectedType)) {
			if (Integer.class.equals(type)) {
				return value;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return Integer.valueOf(((Number) value).intValue());
			}
			else if (Date.class.isAssignableFrom(type)) {
				return Integer.valueOf((int) ((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type)) {
				return Integer.valueOf((int) Double.parseDouble((String) value));
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return (Boolean) value ? Integer.valueOf(1) : Integer.valueOf(0);
			}
		}
		else if (Float.class.equals(expectedType) || Float.TYPE.equals(expectedType)) {
			if (Float.class.equals(type)) {
				return value;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return Float.valueOf(((Number) value).floatValue());
			}
			else if (Date.class.isAssignableFrom(type)) {
				return Float.valueOf(((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type)) {
				return Float.valueOf((float) Double.parseDouble((String) value));
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return (Boolean) value ? Float.valueOf(1) : Float.valueOf(0);
			}
		}
		else if (Short.class.equals(expectedType) || Short.TYPE.equals(expectedType)) {
			if (Short.class.equals(type)) {
				return value;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return Short.valueOf(((Number) value).shortValue());
			}
			else if (Date.class.isAssignableFrom(type)) {
				return Short.valueOf((short) ((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type)) {
				return Short.valueOf((short) Double.parseDouble((String) value));
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return (Boolean) value ? Short.valueOf((short) 1) : Short.valueOf((short) 0);
			}
		}
		else if (Byte.class.equals(expectedType) || Byte.TYPE.equals(expectedType)) {
			if (Byte.class.equals(type)) {
				return value;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return Byte.valueOf(((Number) value).byteValue());
			}
			else if (Date.class.isAssignableFrom(type)) {
				return Byte.valueOf((byte) ((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type)) {
				return Byte.valueOf((byte) Double.parseDouble((String) value));
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return (Boolean) value ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0);
			}
		}
		else if (Boolean.class.equals(expectedType) || Boolean.TYPE.equals(expectedType)) {
			if (Boolean.class.equals(type)) {
				return value;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return Boolean.valueOf(((Number) value).byteValue() != 0);
			}
			else if (Date.class.isAssignableFrom(type)) {
				return Boolean.valueOf((byte) ((Date) value).getTime() != 0);
			}
			else if (String.class.isAssignableFrom(type)) {
				return Boolean.valueOf((String) value);
			}
		}
		else if (Character.class.equals(expectedType) || Character.TYPE.equals(expectedType)) {
			if (Character.class.equals(type)) {
				return value;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return Character.valueOf((char) ((Number) value).intValue());
			}
			else if (String.class.isAssignableFrom(type)) {
				String stringValue = (String) value;
				if (stringValue.length() > 0) {
					return Character.valueOf(stringValue.charAt(0));
				}
				return null;
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return (Boolean) value ? Character.valueOf((char) 1) : Character.valueOf((char) 0);
			}
		}
		else if (Date.class.equals(expectedType)) {
			if (java.sql.Date.class.isAssignableFrom(type)) {
				return new Date(((java.sql.Date) value).getTime());
			}
			else if (XMLGregorianCalendar.class.isAssignableFrom(type)) {
				return ((XMLGregorianCalendar) value).toGregorianCalendar().getTime();
			}
			else if (GregorianCalendar.class.isAssignableFrom(type)) {
				return ((GregorianCalendar) value).getTime();
			}
			else if (String.class.isAssignableFrom(type)) {
				try {
					return getISO_8601_DateFormat().parse((String) value);
				}
				catch (ParseException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			else if (Number.class.isAssignableFrom(type)) {
				return new Date(((Number) value).longValue());
			}
		}
		else if (XMLGregorianCalendar.class.equals(expectedType)) {
			if (Date.class.isAssignableFrom(type)) {
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
		}
		else if (Calendar.class.equals(expectedType) || GregorianCalendar.class.equals(expectedType)) {
			if (String.class.isAssignableFrom(type)) {
				try {
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(getISO_8601_DateFormat().parse((String) value));
					return calendar;
				}
				catch (ParseException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			else if (Date.class.isAssignableFrom(type)) {
				GregorianCalendar c = new GregorianCalendar();
				c.setTime((Date) value);
				return c;
			}
		}
		else if (java.sql.Date.class.equals(expectedType)) {
			if (Date.class.isAssignableFrom(type)) {
				return new java.sql.Date(((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type)) {
				Date date;
				try {
					date = getISO_8601_DateFormat().parse((String) value);
				}
				catch (ParseException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
				return new java.sql.Date(date.getTime());
			}
			else if (Number.class.isAssignableFrom(type)) {
				return new java.sql.Date(((Number) value).longValue());
			}
		}
		else if (java.sql.Timestamp.class.equals(expectedType)) {
			if (Number.class.isAssignableFrom(type)) {
				return new java.sql.Timestamp(((Number) value).longValue());
			}
			else if (java.util.Date.class.isAssignableFrom(type)) {
				return new java.sql.Timestamp(((java.util.Date) value).getTime());
			}
			else if (java.util.Calendar.class.isAssignableFrom(type)) {
				return new java.sql.Timestamp(((java.util.Calendar) value).getTimeInMillis());
			}
		}
		else if (expectedType.isEnum()) {
			if (String.class.isAssignableFrom(type)) {
				try {
					return enumValueOf.invoke(null, expectedType, value);
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			else if (Enum.class.isAssignableFrom(type)) {
				try {
					return convertValueToTypeIntern(expectedType, ((Enum<?>) value).name(),
							additionalInformation);
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		else if (Class.class.isAssignableFrom(expectedType)) {
			if (String.class.isAssignableFrom(type)) {
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
					return Class.forName(sValue, true, classLoaderProvider.getClassLoader());
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		else if (CharSequence.class.isAssignableFrom(expectedType)) {
			if (Class.class.isAssignableFrom(type)) {
				return ((Class<?>) value).getName();
			}
			if (additionalInformation instanceof XmlHint) {
				if (Date.class.isAssignableFrom(type)) {
					return getISO_8601_DateFormat().format(value);
				}
			}
			if (Calendar.class.isAssignableFrom(type)) {
				return getISO_8601_DateFormat().format(((Calendar) value).getTime());
			}
			return value.toString();
		}
		else if (char[].class.equals(expectedType)) {
			if (CharSequence.class.isAssignableFrom(type)) {
				return value.toString().toCharArray();
			}
		}
		throw new IllegalArgumentException(
				"Cannot convert from '" + value.getClass() + "' to '" + expectedType + "'");
	}
}
