package com.koch.ambeth.start;

/*-
 * #%L
 * jambeth-core-test
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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.core.start.SystemClasspathInfo;
import com.koch.ambeth.util.collections.IList;

public class SystemClasspathInfoTest {
	private SystemClasspathInfo systemClasspathInfo;

	@Before
	public void setUp() throws Exception {
		systemClasspathInfo = new SystemClasspathInfo();
	}

	@Test
	public void testGetJarURLs() {
		IList<URL> jarURLs = systemClasspathInfo.getJarURLs();
		Assert.assertNotNull(jarURLs);
		Assert.assertFalse(jarURLs.isEmpty());
	}

	@Test
	public void testOpenAsFile() throws Throwable {
		IList<URL> jarURLs = systemClasspathInfo.getJarURLs();
		URL url = jarURLs.get(jarURLs.size() - 1);
		Path file = systemClasspathInfo.openAsFile(url);
		Assert.assertNotNull(file);
		Assert.assertTrue(Files.isReadable(file));
	}

	@Test
	public void testOpenAsFile_pathWithSpace() throws Throwable {
		String filePath = "file:/home/user/name with space/lib";
		URL url = new URL(filePath);

		Path file = systemClasspathInfo.openAsFile(url);
		Assert.assertNotNull(file);
	}
}
