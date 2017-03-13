package com.koch.ambeth.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.util.FileExistsCache;
import com.koch.ambeth.ioc.util.IFileExistsCache;
import com.koch.ambeth.ioc.util.IPathMonitorConfiguration;
import com.koch.ambeth.log.io.FileUtil;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.category.PerformanceTests;
import com.koch.ambeth.util.FileExistsCacheTest.FileExistsCacheTestModule;

@TestModule(FileExistsCacheTestModule.class)
public class FileExistsCacheTest extends AbstractIocTest {
	public static class FileExistsCacheTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(FileExistsCache.class).autowireable(IFileExistsCache.class,
					IPathMonitorConfiguration.class);
		}
	}

	@Autowired
	protected IFileExistsCache fileExistsCache;

	@Autowired
	protected IPathMonitorConfiguration fileExistsCacheConfiguration;

	@Test
	public void testCreate() throws Throwable {
		Path tempDir = Files.createTempDirectory(getClass().getSimpleName());
		Files.createDirectories(tempDir);
		try {
			fileExistsCacheConfiguration.registerForExists(tempDir);

			// Path child = tempDir.resolve("child");
			Path child = tempDir.resolve("child");
			Path child2 = child.resolve("child2");

			Assert.assertFalse(fileExistsCache.exists(child));
			Assert.assertFalse(fileExistsCache.exists(child));

			Files.createDirectories(child);

			Files.createDirectories(child2);
			Thread.sleep(1000);

			Assert.assertTrue(fileExistsCache.exists(child));
			Assert.assertTrue(fileExistsCache.exists(child2));
		}
		finally {
			FileUtil.deleteRecursive(tempDir, true);
		}
	}

	@Category(PerformanceTests.class)
	@Test
	public void testPerformance() {
		Path path = Paths.get(".");
		fileExistsCacheConfiguration.registerForExists(path);

		int count = 100000;

		long start = System.currentTimeMillis();
		for (int a = count; a-- > 0;) {
			path = Paths.get(".");
			Files.exists(path);
		}
		long end1 = System.currentTimeMillis();
		for (int a = count; a-- > 0;) {
			path = Paths.get(".");
			fileExistsCache.exists(path);
		}
		long end2 = System.currentTimeMillis();

		Assert.assertTrue((end1 - start) + " vs. " + (end2 - end1), (end1 - start) > (end2 - end1) * 2);
	}
}
