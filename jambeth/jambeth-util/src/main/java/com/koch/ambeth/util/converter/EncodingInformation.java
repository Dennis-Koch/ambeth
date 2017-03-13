package com.koch.ambeth.util.converter;

public final class EncodingInformation
{
	public static final int SOURCE_PLAIN = 0, SOURCE_HEX = 1, SOURCE_BASE64 = 2, TARGET_PLAIN = 0, TARGET_HEX = 4, TARGET_BASE64 = 8;

	public static EncodingType getSourceEncoding(int encoding)
	{
		if ((encoding & SOURCE_HEX) != 0)
		{
			return EncodingType.HEX;
		}
		else if ((encoding & SOURCE_BASE64) != 0)
		{
			return EncodingType.BASE64;
		}
		return EncodingType.PLAIN;
	}

	public static EncodingType getTargetEncoding(int encoding)
	{
		if ((encoding & TARGET_HEX) != 0)
		{
			return EncodingType.HEX;
		}
		else if ((encoding & TARGET_BASE64) != 0)
		{
			return EncodingType.BASE64;
		}
		return EncodingType.PLAIN;
	}

	private EncodingInformation()
	{
		// Intended blank
	}
}
