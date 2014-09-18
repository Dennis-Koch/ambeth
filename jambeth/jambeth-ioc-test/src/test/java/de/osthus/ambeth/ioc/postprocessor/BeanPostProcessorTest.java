package de.osthus.ambeth.ioc.postprocessor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.ioc.IBeanPostProcessor;
import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

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
