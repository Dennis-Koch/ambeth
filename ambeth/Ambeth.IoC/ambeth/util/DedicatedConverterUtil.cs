using De.Osthus.Ambeth.Ioc.Factory;
using System;

namespace De.Osthus.Ambeth.Util
{
    public class DedicatedConverterUtil
    {
        private DedicatedConverterUtil()
        {
            // Intended blank
        }

        public static void BiLink(IBeanContextFactory beanContextFactory, String listenerBeanName, Type fromType, Type toType)
        {
            beanContextFactory.Link(listenerBeanName).To<IDedicatedConverterExtendable>().With(fromType, toType);
            beanContextFactory.Link(listenerBeanName).To<IDedicatedConverterExtendable>().With(toType, fromType);
        }
    }
}
