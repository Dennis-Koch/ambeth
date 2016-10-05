package de.osthus.ambeth.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.io.FileUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.util.FileExistsCacheTest.FileExistsCacheTestModule;

@TestModule(FileExistsCacheTestModule.class)
public class FileExistsCacheTest extends AbstractIocTest
{
	public static class FileExistsCacheTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(FileExistsCache.class).autowireable(IFileExistsCache.class, IPathMonitorConfiguration.class);
		}
	}

	@Autowired
	protected IFileExistsCache fileExistsCache;

	@Autowired
	protected IPathMonitorConfiguration fileExistsCacheConfiguration;

	@Test
	public void testCreate() throws IOException
	{
		Path tempDir = Files.createTempDirectory(getClass().getSimpleName());
		Files.createDirectories(tempDir);
		try
		{
			fileExistsCacheConfiguration.registerForExists(tempDir);

			// Path child = tempDir.resolve("child");
			Path child = tempDir.resolve("child");
			Path child2 = child.resolve("child2");

			Assert.assertFalse(fileExistsCache.exists(child));
			Assert.assertFalse(fileExistsCache.exists(child));

			Files.createDirectories(child);

			Files.createDirectories(child2);

			Assert.assertTrue(fileExistsCache.exists(child));
			Assert.assertTrue(fileExistsCache.exists(child2));
		}
		finally
		{
			FileUtil.deleteRecursive(tempDir, true);
		}
	}

	@Test
	public void testPerformance()
	{
		Path path = Paths.get(".");
		fileExistsCacheConfiguration.registerForExists(path);

		int count = 100000;

		long start = System.currentTimeMillis();
		for (int a = count; a-- > 0;)
		{
			Files.exists(path);
		}
		long end1 = System.currentTimeMillis();
		for (int a = count; a-- > 0;)
		{
			fileExistsCache.exists(path);
		}
		long end2 = System.currentTimeMillis();

		Assert.assertTrue((end1 - start) > (end2 - end1) * 10);
	}
}
