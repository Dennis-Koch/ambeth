package de.osthus.ambeth.format;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class ISO8601DateFormat extends SimpleDateFormat
{
	private static final long serialVersionUID = -4034139121118690879L;

	private static final Pattern iso8601_preprocessPattern = Pattern.compile(":(?=[0-9]{2}$)");

	public ISO8601DateFormat()
	{
		super("yyyy-MM-dd'T'HH:mm:ssZ");
	}

	@Override
	public Date parse(String source, ParsePosition pos)
	{
		source = iso8601_preprocessPattern.matcher(source).replaceFirst("");
		return super.parse(source, pos);
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos)
	{
		StringBuffer sourceSb = super.format(date, toAppendTo, pos);

		sourceSb.insert(sourceSb.length() - 2, ':');
		return sourceSb;
	}
}
