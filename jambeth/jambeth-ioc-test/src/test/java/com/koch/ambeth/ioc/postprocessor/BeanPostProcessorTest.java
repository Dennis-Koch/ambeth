package com.koch.ambeth.ioc.postprocessor;

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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;

public class BeanPostProcessorTest extends AbstractIocTest
{
	@Test
	@TestModule(BeanPostProcessorTestModule.class)
	@TestProperties(name = BeanPostProcessorTestModule.NumberOfPostProcessors, value = "100")
	public void orderOf100PostProcessors()
	{
		List<IBeanPostProcessor> postProcessors = ((ServiceContext) beanContext).getPostProcessors();
		Assert.assertEquals(100, postProcessors.size());
		for (int a = 0, size = postProcessors.size() - 1; a < size; a++)
		{
			OrderedPostProcessor left = (OrderedPostProcessor) postProcessors.get(a);
			OrderedPostProcessor right = (OrderedPostProcessor) postProcessors.get(a + 1);

			// the left (previous) postProcessor will process a bean BEFORE the right postProcessor
			// for Aspects this means that the LAST aspect will encapsulate the whole call cascade
			// so the LAST Aspect applied is the FIRST (outermost) one called when the bean gets called at runtime
			Assert.assertTrue(left.getOrder().getPosition() >= right.getOrder().getPosition());
		}
	}
}
