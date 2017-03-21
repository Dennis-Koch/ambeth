package com.koch.ambeth.log;

/*-
 * #%L
 * jambeth-log-test
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

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.util.exception.MaskingRuntimeException;

public class LogSQLExceptionTest {
	@Test
	public void test() {
		final StringBuilder sb = new StringBuilder();
		Logger logger = new Logger(LogSQLExceptionTest.class.getName()) {
			@Override
			protected void log(LogLevel logLevel, String output) {
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
