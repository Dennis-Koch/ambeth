package com.koch.ambeth.ioc.converter;

import java.sql.Blob;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;

public class StringToBlobConverter implements IDedicatedConverter
{
	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> targetClass, Object value, Object additionalInformation)
	{
		if (expectedType.equals(Blob.class))
		{
			byte[] bytes = conversionHelper.convertValueToType(byte[].class, value);
			return conversionHelper.convertValueToType(Blob.class, bytes);
		}
		byte[] bytes = conversionHelper.convertValueToType(byte[].class, value);
		return conversionHelper.convertValueToType(String.class, bytes);
	}
}
