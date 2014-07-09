package de.osthus.ambeth.h2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class Functions
{
	// public static int getRows(Connection connection, int number) throws SQLException
	// {
	// // connection
	// }

	public static Timestamp toTimestamp(Connection connection, String value, String format) throws SQLException
	{
		if (!"DD.MM.RR HH24:MI:SS".equals(format))
		{
			throw new IllegalArgumentException("Format not yet supported:" + format);
		}
		try
		{
			return new Timestamp(new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(value).getTime());
		}
		catch (ParseException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
