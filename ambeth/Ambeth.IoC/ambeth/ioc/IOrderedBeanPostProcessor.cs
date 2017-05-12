using System;

namespace De.Osthus.Ambeth.Ioc
{
    public interface IOrderedBeanPostProcessor
    {
        PostProcessorOrder GetOrder();
    }
}
