package de.osthus.ambeth.bytebuffer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.bytebuffer.ByteBufferTest.ByteBufferTestModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.util.ReflectUtil;

@TestModule(ByteBufferTestModule.class)
public class ByteBufferTest extends AbstractIocTest
{
	public static class ByteBufferTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(FileContentCache.class).autowireable(IFileContentCache.class);
			beanContextFactory.registerBean(FileHandleCache.class).autowireable(IFileHandleCache.class);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IFileContentCache fileContentCache;

	@Test
	public void test() throws Throwable
	{
		FileKey fileKey = new FileKeyImpl(Paths.get("./pom.xml").toAbsolutePath().normalize());
		ByteBuffer[] content = fileContentCache.getContent(fileKey, 10, 10);
		ByteBuffer[] content2 = fileContentCache.getContent(fileKey, 10, 10);
		Assert.assertEquals(1, content.length);
		Assert.assertEquals(content[0].remaining(), 10);

		Assert.assertEquals(1, content2.length);
		Assert.assertEquals(content2[0].remaining(), 10);

		// check for same (cached) backing bytebuffer
		Class<?> clazz = content[0].getClass();
		Field f_att = null;
		while (f_att == null)
		{
			f_att = ReflectUtil.getDeclaredField(clazz, "att");
			clazz = clazz.getSuperclass();
		}
		Assert.assertSame(f_att.get(content[0]), f_att.get(content2[0]));
	}
}
