package com.koch.ambeth.config;

/*-
 * #%L
 * jambeth-test
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.testutil.AbstractIocTest;

public class PropertyResolverTest extends AbstractIocTest
{

	@LogInstance
	private ILogger log;
	private Properties props;

	@Before
	public void setupProperties()
	{
		props = new Properties();
		props.putString("knownVar", "123");
		props.putString("partOfVar", "Var");
		props.putString("hugoVar", "known${partOfVar}");
		props.putString("cycle", "${cycle}");
		props.putString("nextUnknown", "${unknownVar}");

	}

	@Test
	public void testCorrectResolve()
	{
		Assert.assertEquals("abc123abc123", props.resolvePropertyParts("abc${knownVar}abc${knownVar}"));
		Assert.assertEquals("abcVar", props.resolvePropertyParts("abc${partOfVar}"));
		Assert.assertEquals("abcknownVar", props.resolvePropertyParts("abc${hugoVar}"));
		Assert.assertEquals("abc123", props.resolvePropertyParts("abc${${hugoVar}}"));

		// test no resolve
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCycleException()
	{
		props.resolvePropertyParts("${cycle}");

	}

	@Test
	public void testUnknownProperty()
	{
		// Assert.assertEquals("abc${unknownVar}", props.resolvePropertyParts("abc${unknownVar}"));
		// Assert.assertEquals("test${unknownVar}testabc123test${unknownVar}qwer",
		// props.resolvePropertyParts("test${unknownVar}testabc${${hugoVar}}test${unknownVar}qwer"));

		Assert.assertEquals("test${unknownVar}testabc123test${unknownVar}qwerA${unknownVar}qwerB",
				props.resolvePropertyParts("test${unknownVar}testabc${${hugoVar}}test${unknownVar}qwerA${unknownVar}qwerB"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownPropertyWithCycleException()
	{
		props.resolvePropertyParts("test${unknownVar}testabc${cycle}${${hugoVar}}test${unknownVar}qwer${unknownVar}qwer");
	}

	@Test
	public void testUnknownPropertySecondResolve()
	{
		// here nextUnknown is "known" but the next step "unknown" variable is not known, that means that nextUnknown can't be resolved completely and
		// nextUnknown goes back into the string
		Assert.assertEquals("abc${unknownVar}", props.resolvePropertyParts("abc${nextUnknown}"));
		Assert.assertEquals("${unknownVar}", props.get("nextUnknown"));

		Assert.assertNull(props.get("unknownVar"));
		Assert.assertNotNull(props.get("nextUnknown"));

		Assert.assertEquals("abc${unknownVar}asdfasdfsadf", props.resolvePropertyParts("abc${nextUnknown}asdfasdfsadf"));
		Assert.assertEquals("abc${unknownVar}asdfasdfVar", props.resolvePropertyParts("abc${nextUnknown}asdfasdf${partOfVar}"));
	}

}
