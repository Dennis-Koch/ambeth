package de.osthus.ambeth.log;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.io.FileUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.log.LogFileHandleCache.LoggerStream;
import de.osthus.ambeth.testutil.AbstractIocTest;

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

		loggerStream1_a.writer.flush();
		loggerStream2_a.writer.flush();
	}
}
