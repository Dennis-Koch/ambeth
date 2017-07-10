package com.koch.ambeth.cache.stream;

/*-
 * #%L
 * jambeth-cache-stream-test
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

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.cache.stream.ByteBufferTest.ByteBufferTestModule;
import com.koch.ambeth.cache.stream.bytebuffer.FileContentCache;
import com.koch.ambeth.cache.stream.bytebuffer.FileHandleCache;
import com.koch.ambeth.cache.stream.bytebuffer.FileKey;
import com.koch.ambeth.cache.stream.bytebuffer.FileKeyImpl;
import com.koch.ambeth.cache.stream.bytebuffer.IFileContentCache;
import com.koch.ambeth.cache.stream.bytebuffer.IFileHandleCache;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.util.ReflectUtil;

@TestModule(ByteBufferTestModule.class)
public class ByteBufferTest extends AbstractIocTest {
	public static class ByteBufferTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(FileContentCache.class).autowireable(IFileContentCache.class);
			beanContextFactory.registerBean(FileHandleCache.class).autowireable(IFileHandleCache.class);
		}
	}

	@Autowired
	protected IFileContentCache fileContentCache;

	@Test
	public void test() throws Throwable {
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
		while (f_att == null) {
			f_att = ReflectUtil.getDeclaredField(clazz, "att");
			clazz = clazz.getSuperclass();
		}
		Assert.assertSame(f_att.get(content[0]), f_att.get(content2[0]));
	}
}
