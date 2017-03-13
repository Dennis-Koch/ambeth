package com.koch.ambeth.log;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.log.LogLevel;
import com.koch.ambeth.log.Logger;
import com.koch.ambeth.util.exception.MaskingRuntimeException;

public class LogSQLExceptionTest
{
	@Test
	public void test()
	{
		final StringBuilder sb = new StringBuilder();
		Logger logger = new Logger(LogSQLExceptionTest.class.getName())
		{
			@Override
			protected void log(LogLevel logLevel, String output)
			{
				sb.append(output);
			}
		};

		String reason = "##myReason###", reason2 = "##myReason2###";

		SQLException sqlEx = new SQLException(reason);
		sqlEx.setNextException(new SQLException(reason2));
		logger.error(new MaskingRuntimeException(sqlEx));

		Assert.assertTrue(sb.toString().contains(reason));
		Assert.assertTrue(sb.toString().contains(reason2));
	}
}
