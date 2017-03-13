package com.koch.ambeth.util.converter;

import java.nio.charset.Charset;

import com.koch.ambeth.util.Base64;

public abstract class AbstractEncodingArrayConverter
{
	public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

	protected EncodingType getSourceEncoding(Object additionalInformation)
	{
		EncodingType sourceEncoding = EncodingType.PLAIN;
		if (additionalInformation instanceof Integer)
		{
			int encoding = ((Integer) additionalInformation).intValue();
			sourceEncoding = EncodingInformation.getSourceEncoding(encoding);
		}
		return sourceEncoding;
	}

	protected EncodingType getTargetEncoding(Object additionalInformation)
	{
		EncodingType targetEncoding = EncodingType.PLAIN;
		if (additionalInformation instanceof Integer)
		{
			int encoding = ((Integer) additionalInformation).intValue();
			targetEncoding = EncodingInformation.getTargetEncoding(encoding);
		}
		return targetEncoding;
	}

	protected byte[] switchEncoding(byte[] bytes, EncodingType sourceEncoding, EncodingType targetEncoding)
	{
		if (sourceEncoding.equals(targetEncoding))
		{
			// Only type conversion necessary
			return bytes;
		}
		switch (sourceEncoding)
		{
			case PLAIN:
			{
				// Nothing to do
				break;
			}
			case BASE64:
			{
				bytes = Base64.decodeBase64(bytes);
				break;
			}
			default:
			{
				throw new UnsupportedOperationException(sourceEncoding + " not yet supported");
			}
		}
		switch (targetEncoding)
		{
			case PLAIN:
			{
				// Nothing to do
				break;
			}
			case BASE64:
			{
				bytes = Base64.encodeBase64(bytes);
				break;
			}
			default:
			{
				throw new UnsupportedOperationException(sourceEncoding + " not yet supported");
			}
		}
		return bytes;
	}
}
