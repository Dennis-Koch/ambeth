package com.koch.ambeth.server.helloworld.setup;

/*-
 * #%L
 * jambeth-server-helloworld
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

import java.io.File;

import org.junit.Test;

import com.koch.ambeth.server.helloworld.AbstractHelloWorldTest;

public class RebuildSchema {
	public static class DummyTest extends AbstractHelloWorldTest {
		@Test
		public void dummyTest() {
			// noop
		}
	}

	public static void main(String[] args) throws Exception {
		// String propertyFileToUse = args[0];
		String propertyFileToUse =
				"../jambeth-server-helloworld/src/main/resources/helloworld.properties";

		if (propertyFileToUse.startsWith("/") || propertyFileToUse.contains(":")) {
			// is absolute
			System.setProperty("property.file", propertyFileToUse);
		}
		else {
			// is relative
			System.setProperty("property.file",
					new File("").getAbsolutePath() + File.separator + propertyFileToUse);
		}
		com.koch.ambeth.testutil.RebuildSchema.main(args, DummyTest.class, propertyFileToUse);
	}
}
