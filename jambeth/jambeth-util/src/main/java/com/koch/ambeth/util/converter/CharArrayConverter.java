package com.koch.ambeth.util.converter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import com.koch.ambeth.util.IDedicatedConverter;

public class CharArrayConverter extends AbstractEncodingArrayConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		EncodingType sourceEncoding = getSourceEncoding(additionalInformation);
		EncodingType targetEncoding = getTargetEncoding(additionalInformation);

		if (char[].class.equals(sourceType) && CharSequence.class.isAssignableFrom(expectedType))
		{
			ByteBuffer byteBuffer = CHARSET_UTF8.encode(CharBuffer.wrap((char[]) value));
			byte[] bytes = new byte[byteBuffer.limit()];
			byteBuffer.get(bytes);
			bytes = switchEncoding(bytes, sourceEncoding, targetEncoding);
			String text = new String(bytes, CHARSET_UTF8);
			return text;
		}
		else if (String.class.equals(sourceType) && char[].class.equals(expectedType))
		{
			byte[] bytes = ((String) value).getBytes(CHARSET_UTF8);
			bytes = switchEncoding(bytes, sourceEncoding, targetEncoding);
			CharBuffer charBuffer = CHARSET_UTF8.decode(ByteBuffer.wrap(bytes));
			char[] chars = new char[charBuffer.limit()];
			charBuffer.get(chars);
			return chars;
		}
		else if (CharSequence.class.isAssignableFrom(sourceType) && char[].class.equals(expectedType))
		{
			byte[] bytes = ((CharSequence) value).toString().getBytes(CHARSET_UTF8);
			bytes = switchEncoding(bytes, sourceEncoding, targetEncoding);
			CharBuffer charBuffer = CHARSET_UTF8.decode(ByteBuffer.wrap(bytes));
			char[] chars = new char[charBuffer.limit()];
			charBuffer.get(chars);
			return chars;
		}
		throw new IllegalStateException("Conversion " + sourceType.getName() + "->" + expectedType.getName() + " not supported");
	}
}
