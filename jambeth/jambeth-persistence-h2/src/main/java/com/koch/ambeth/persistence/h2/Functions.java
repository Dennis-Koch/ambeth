package com.koch.ambeth.persistence.h2;

/*-
 * #%L
 * jambeth-persistence-h2
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class Functions
{
	// public static int getRows(Connection connection, int number) throws SQLException
	// {
	// // connection
	// }

	public static Timestamp toTimestamp(Connection connection, String value, String format) throws SQLException
	{
		try
		{
			if ("DD.MM.RR HH24:MI:SS".equals(format))
			{
				return new Timestamp(new SimpleDateFormat("dd.MM.yy HH:mm:ss").parse(value).getTime());
			}
			else if ("DD.MM.RR HH24:MI:SS,FF".equals(format))
			{
				return new Timestamp(new SimpleDateFormat("dd.MM.yy HH:mm:ss,SS").parse(value).getTime());
			}
		}
		catch (ParseException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		throw new IllegalArgumentException("Format not yet supported:" + format);
	}
}
