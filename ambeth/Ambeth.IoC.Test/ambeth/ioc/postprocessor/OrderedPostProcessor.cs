using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Postprocessor
{
    public class OrderedPostProcessor : IBeanPostProcessor, IOrderedBeanPostProcessor
    {
        [Property]
        public PostProcessorOrder Order { protected get; set; }
                
        public PostProcessorOrder GetOrder()
        {
            return Order;
        }

        public Object PostProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, Config.IBeanConfiguration beanConfiguration, System.Type beanType, Object targetBean, ISet<Type> requestedTypes)
        {
            return targetBean;
        }
    }
}