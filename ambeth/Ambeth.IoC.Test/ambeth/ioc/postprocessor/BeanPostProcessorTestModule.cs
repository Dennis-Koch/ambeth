using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Log;
using System;

namespace De.Osthus.Ambeth.Ioc.Postprocessor
{
    public class BeanPostProcessorTestModule : IInitializingModule
    {
        public const String NumberOfPostProcessors = "numberOfPostProcessors";

        [LogInstance]
        public ILogger Log { private get; set; }

        [Property(NumberOfPostProcessors)]
        public int Number { protected get; set; }

        public void AfterPropertiesSet(Factory.IBeanContextFactory beanContextFactory)
        {
            PostProcessorOrder[] orders = PostProcessorOrder.Values;
            Random rand = new Random();
            for (int a = Number; a-- > 0; )
            {
                PostProcessorOrder order = orders[rand.Next(orders.Length)];
                beanContextFactory.RegisterBean<OrderedPostProcessor>().PropertyValue("Order", order);
            }
        }
    }
}