package com.koch.ambeth.util.format;

import java.text.DecimalFormat;
import java.text.FieldPosition;

public class PaddingDecimalFormat extends DecimalFormat
{
	private static final long serialVersionUID = -7737645664654567172L;

	private final int leadingWidth;

	private final String decimalSeparator;

	public PaddingDecimalFormat(String pattern)
	{
		super(pattern);
		int leadingWidth = 0;
		for (int a = 0, size = pattern.length(); a < size; a++)
		{
			char oneChar = pattern.charAt(a);
			leadingWidth++;
			if (oneChar != '#' && oneChar != '0')
			{
				break;
			}
		}
		this.leadingWidth = leadingWidth;
		decimalSeparator = "" + getDecimalFormatSymbols().getDecimalSeparator();
	}

	@Override
	public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition)
	{
		StringBuffer sb = super.format(number, result, fieldPosition);
		int indexOf = sb.indexOf(decimalSeparator);
		int leadingWidth = indexOf < this.leadingWidth ? this.leadingWidth : indexOf;
		while (indexOf < leadingWidth)
		{
			sb.insert(0, ' ');
			indexOf++;
		}
		return sb;
	}

	@Override
	public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition)
	{
		StringBuffer sb = super.format(number, result, fieldPosition);
		while (sb.length() < leadingWidth)
		{
			sb.insert(0, ' ');
		}
		return sb;
	}
}