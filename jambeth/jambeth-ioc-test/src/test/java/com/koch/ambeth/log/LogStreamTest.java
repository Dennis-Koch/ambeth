package com.koch.ambeth.log;

/*-
 * #%L
 * jambeth-ioc-test
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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogConfigurationConstants;
import com.koch.ambeth.log.LogFileHandleCache;
import com.koch.ambeth.log.LogFileHandleCache.LoggerStream;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.log.io.FileUtil;
import com.koch.ambeth.testutil.AbstractIocTest;

public class LogStreamTest extends AbstractIocTest
{
	@Autowired
	protected ILoggerCache loggerCache;

	@Test
	public void testStream() throws Throwable
	{
		Path tempDir = Paths.get("temp");

		Path file1 = tempDir.resolve("file1.txt");
		Path file2 = tempDir.resolve("file2.txt");

		FileUtil.deleteRecursive(file1, false);
		FileUtil.deleteRecursive(file2, false);

		Properties props1 = new Properties();
		props1.put(LoggerFactory.logLevelPropertyPrefix, "debug");
		props1.put(LogConfigurationConstants.LogFile, file1);

		Properties props2 = new Properties();
		props2.put(LoggerFactory.logLevelPropertyPrefix, "debug");
		props2.put(LogConfigurationConstants.LogFile, file2);

		IServiceContext context1 = BeanContextFactory.createBootstrap(props1, IocModule.class);
		IServiceContext context2 = BeanContextFactory.createBootstrap(props2, IocModule.class);

		ILogger log1 = context1.getService(ILoggerCache.class).getCachedLogger(props1, LogStreamTest.class);
		ILogger log2 = context2.getService(ILoggerCache.class).getCachedLogger(props2, LogStreamTest.class);

		log1.debug("Test1");
		log2.debug("Test2");

		LoggerStream loggerStream1_a = LogFileHandleCache.getSharedWriter(file1.toAbsolutePath());
		LoggerStream loggerStream1_b = LogFileHandleCache.getSharedWriter(file1);

		LoggerStream loggerStream2_a = LogFileHandleCache.getSharedWriter(file2.toAbsolutePath());
		LoggerStream loggerStream2_b = LogFileHandleCache.getSharedWriter(file2);

		Assert.assertSame(loggerStream1_a, loggerStream1_b);
		Assert.assertSame(loggerStream2_a, loggerStream2_b);
		Assert.assertNotSame(loggerStream1_a, loggerStream2_a);

		loggerStream1_a.getWriter().flush();
		loggerStream2_a.getWriter().flush();
	}
}
