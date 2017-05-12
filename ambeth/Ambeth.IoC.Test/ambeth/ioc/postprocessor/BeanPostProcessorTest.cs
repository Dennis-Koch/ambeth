using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Postprocessor
{
    [TestClass]
    public class BeanPostProcessorTest : AbstractIocTest
    {
        [TestMethod]
        [TestModule(typeof(BeanPostProcessorTestModule))]
        [TestProperties(Name = BeanPostProcessorTestModule.NumberOfPostProcessors, Value = "100")]
        public void OrderOf100PostProcessors()
        {
            IList<IBeanPostProcessor> postProcessors = ((ServiceContext)BeanContext).GetPostProcessors();
            Assert.AssertEquals(100, postProcessors.Count);
            for (int a = 0, size = postProcessors.Count - 1; a < size; a++)
            {
                OrderedPostProcessor left = (OrderedPostProcessor)postProcessors[a];
                OrderedPostProcessor right = (OrderedPostProcessor)postProcessors[a + 1];

                // the left (previous) postProcessor will process a bean BEFORE the right postProcessor
                // for Aspects this means that the LAST aspect will encapsulate the whole call cascade
                // so the LAST Aspect applied is the FIRST (outermost) one called when the bean gets called at runtime
                Assert.AssertTrue(left.GetOrder().Position >= right.GetOrder().Position);
            }
        }
    }
}
