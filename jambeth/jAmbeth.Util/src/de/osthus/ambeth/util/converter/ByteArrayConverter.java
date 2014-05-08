package de.osthus.ambeth.util.converter;

import de.osthus.ambeth.util.IDedicatedConverter;

public class ByteArrayConverter extends AbstractEncodingArrayConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		EncodingType sourceEncoding = getSourceEncoding(additionalInformation);
		EncodingType targetEncoding = getTargetEncoding(additionalInformation);

		if (byte[].class.equals(sourceType) && String.class.equals(expectedType))
		{
			byte[] bytes = switchEncoding((byte[]) value, sourceEncoding, targetEncoding);
			String text = new String(bytes, CHARSET_UTF8);
			return text;
		}
		else if (String.class.equals(sourceType) && byte[].class.equals(expectedType))
		{
			byte[] bytes = ((String) value).getBytes(CHARSET_UTF8);
			bytes = switchEncoding(bytes, sourceEncoding, targetEncoding);
			return bytes;
		}
		throw new IllegalStateException("Conversion " + sourceType.getName() + "->" + expectedType.getName() + " not supported");
	}
}
