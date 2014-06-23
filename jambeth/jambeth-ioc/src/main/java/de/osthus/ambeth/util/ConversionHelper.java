package de.osthus.ambeth.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.format.ISO8601DateFormat;
import de.osthus.ambeth.format.XmlHint;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class ConversionHelper implements IConversionHelper, IThreadLocalCleanupBean
{
	protected static final boolean java7recognized = "1.7".equals(System.getProperty("java.specification.version"));

	protected final Method enumValueOf;

	protected final ThreadLocal<DateFormat> iso8601_DateFormatTL = new SensitiveThreadLocal<DateFormat>();

	public ConversionHelper()
	{
		try
		{
			enumValueOf = Enum.class.getMethod("valueOf", Class.class, String.class);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void cleanupThreadLocal()
	{
		iso8601_DateFormatTL.remove();
	}

	protected DateFormat getISO_8601_DateFormat()
	{
		DateFormat dateFormat = iso8601_DateFormatTL.get();
		if (dateFormat == null)
		{
			if (java7recognized)
			{
				dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
			}
			else
			{
				dateFormat = new ISO8601DateFormat();
			}
			iso8601_DateFormatTL.set(dateFormat);
		}
		return dateFormat;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convertValueToType(Class<T> expectedType, Object value)
	{
		return (T) convertValueToTypeIntern(expectedType, value, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convertValueToType(Class<T> expectedType, Object value, Object additionalInformation)
	{
		return (T) convertValueToTypeIntern(expectedType, value, additionalInformation);
	}

	protected Object convertValueToTypeIntern(Class<?> expectedType, Object value, Object additionalInformation)
	{
		if (expectedType == null || value == null)
		{
			return value;
		}
		Class<?> type = value.getClass();
		if (expectedType.isAssignableFrom(type))
		{
			return value;
		}
		if (BigInteger.class.equals(expectedType))
		{
			if (BigDecimal.class.isAssignableFrom(type))
			{
				return ((BigDecimal) value).toBigInteger();
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return BigInteger.valueOf(((Number) value).longValue());
			}
			else if (String.class.isAssignableFrom(type))
			{
				return new BigInteger((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return BigInteger.valueOf((Boolean) value ? 1 : 0);
			}
		}
		else if (Long.class.equals(expectedType) || Long.TYPE.equals(expectedType))
		{
			if (Long.class.equals(type))
			{
				return value;
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return Long.valueOf(((Number) value).longValue());
			}
			else if (Date.class.isAssignableFrom(type))
			{
				return Long.valueOf(((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type))
			{
				return Long.valueOf((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return (Boolean) value ? Long.valueOf(1) : Long.valueOf(0);
			}
		}
		else if (BigDecimal.class.equals(expectedType))
		{
			if (Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type))
			{
				return BigDecimal.valueOf(((Number) value).doubleValue()).stripTrailingZeros();
			}
			else if (BigInteger.class.isAssignableFrom(type))
			{
				return new BigDecimal((BigInteger) value);
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return BigDecimal.valueOf(((Number) value).longValue());
			}
			else if (String.class.isAssignableFrom(type))
			{
				return new BigDecimal((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return BigDecimal.valueOf((Boolean) value ? 1 : 0);
			}
		}
		else if (Double.class.equals(expectedType) || Double.TYPE.equals(expectedType))
		{
			if (Double.class.equals(type))
			{
				return value;
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return Double.valueOf(((Number) value).doubleValue());
			}
			else if (Date.class.isAssignableFrom(type))
			{
				return Double.valueOf(((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type))
			{
				return Double.valueOf((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return (Boolean) value ? Double.valueOf(1) : Double.valueOf(0);
			}
		}
		else if (Integer.class.equals(expectedType) || Integer.TYPE.equals(expectedType))
		{
			if (Integer.class.equals(type))
			{
				return value;
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return Integer.valueOf(((Number) value).intValue());
			}
			else if (Date.class.isAssignableFrom(type))
			{
				return Integer.valueOf((int) ((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type))
			{
				return Integer.valueOf((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return (Boolean) value ? Integer.valueOf(1) : Integer.valueOf(0);
			}
		}
		else if (Float.class.equals(expectedType) || Float.TYPE.equals(expectedType))
		{
			if (Float.class.equals(type))
			{
				return value;
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return Float.valueOf(((Number) value).floatValue());
			}
			else if (Date.class.isAssignableFrom(type))
			{
				return Float.valueOf(((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type))
			{
				return Float.valueOf((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return (Boolean) value ? Float.valueOf(1) : Float.valueOf(0);
			}
		}
		else if (Short.class.equals(expectedType) || Short.TYPE.equals(expectedType))
		{
			if (Short.class.equals(type))
			{
				return value;
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return Short.valueOf(((Number) value).shortValue());
			}
			else if (Date.class.isAssignableFrom(type))
			{
				return Short.valueOf((short) ((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type))
			{
				return Short.valueOf((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return (Boolean) value ? Short.valueOf((short) 1) : Short.valueOf((short) 0);
			}
		}
		else if (Byte.class.equals(expectedType) || Byte.TYPE.equals(expectedType))
		{
			if (Byte.class.equals(type))
			{
				return value;
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return Byte.valueOf(((Number) value).byteValue());
			}
			else if (Date.class.isAssignableFrom(type))
			{
				return Byte.valueOf((byte) ((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type))
			{
				return Byte.valueOf((String) value);
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return (Boolean) value ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0);
			}
		}
		else if (Boolean.class.equals(expectedType) || Boolean.TYPE.equals(expectedType))
		{
			if (Boolean.class.equals(type))
			{
				return value;
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return Boolean.valueOf(((Number) value).byteValue() != 0);
			}
			else if (Date.class.isAssignableFrom(type))
			{
				return Boolean.valueOf((byte) ((Date) value).getTime() != 0);
			}
			else if (String.class.isAssignableFrom(type))
			{
				return Boolean.valueOf((String) value);
			}
		}
		else if (Character.class.equals(expectedType) || Character.TYPE.equals(expectedType))
		{
			if (Character.class.equals(type))
			{
				return value;
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return Character.valueOf((char) ((Number) value).intValue());
			}
			else if (String.class.isAssignableFrom(type))
			{
				String stringValue = (String) value;
				if (stringValue.length() > 0)
				{
					return Character.valueOf(stringValue.charAt(0));
				}
				return null;
			}
			else if (Boolean.class.isAssignableFrom(type))
			{
				return (Boolean) value ? Character.valueOf((char) 1) : Character.valueOf((char) 0);
			}
		}
		else if (Date.class.equals(expectedType))
		{
			if (java.sql.Date.class.isAssignableFrom(type))
			{
				return new Date(((java.sql.Date) value).getTime());
			}
			else if (XMLGregorianCalendar.class.isAssignableFrom(type))
			{
				return ((XMLGregorianCalendar) value).toGregorianCalendar().getTime();
			}
			else if (GregorianCalendar.class.isAssignableFrom(type))
			{
				return ((GregorianCalendar) value).getTime();
			}
			else if (String.class.isAssignableFrom(type))
			{
				try
				{
					return getISO_8601_DateFormat().parse((String) value);
				}
				catch (ParseException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return new Date(((Number) value).longValue());
			}
		}
		else if (XMLGregorianCalendar.class.equals(expectedType))
		{
			if (Date.class.isAssignableFrom(type))
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime((Date) value);
				try
				{
					XMLGregorianCalendar xmlcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
					return xmlcal;
				}
				catch (DatatypeConfigurationException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		else if (GregorianCalendar.class.equals(expectedType))
		{
			if (Date.class.isAssignableFrom(type))
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime((Date) value);
				return c;
			}
		}
		else if (java.sql.Date.class.equals(expectedType))
		{
			if (Date.class.isAssignableFrom(type))
			{
				return new java.sql.Date(((Date) value).getTime());
			}
			else if (String.class.isAssignableFrom(type))
			{
				Date date;
				try
				{
					date = getISO_8601_DateFormat().parse((String) value);
				}
				catch (ParseException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				return new java.sql.Date(date.getTime());
			}
			else if (Number.class.isAssignableFrom(type))
			{
				return new java.sql.Date(((Number) value).longValue());
			}
		}
		else if (java.sql.Timestamp.class.equals(expectedType))
		{
			if (Number.class.isAssignableFrom(type))
			{
				return new java.sql.Timestamp(((Number) value).longValue());
			}
			else if (java.util.Date.class.isAssignableFrom(type))
			{
				return new java.sql.Timestamp(((java.util.Date) value).getTime());
			}
			else if (java.util.Calendar.class.isAssignableFrom(type))
			{
				return new java.sql.Timestamp(((java.util.Calendar) value).getTimeInMillis());
			}
		}
		else if (expectedType.isEnum())
		{
			if (String.class.isAssignableFrom(type))
			{
				try
				{
					return enumValueOf.invoke(null, expectedType, value);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			else if (Enum.class.isAssignableFrom(type))
			{
				try
				{
					return convertValueToTypeIntern(expectedType, ((Enum<?>) value).name(), additionalInformation);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		else if (Class.class.isAssignableFrom(expectedType))
		{
			if (String.class.isAssignableFrom(type))
			{
				try
				{
					return Thread.currentThread().getContextClassLoader().loadClass((String) value);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		else if (String.class.equals(expectedType))
		{
			if (Class.class.isAssignableFrom(type))
			{
				return ((Class<?>) value).getName();
			}
			if (additionalInformation instanceof XmlHint)
			{
				if (Date.class.isAssignableFrom(type))
				{
					return getISO_8601_DateFormat().format(value);
				}
			}
			return value.toString();
		}
		throw new IllegalArgumentException("Cannot convert from '" + value.getClass() + "' to '" + expectedType + "'");
	}
}
