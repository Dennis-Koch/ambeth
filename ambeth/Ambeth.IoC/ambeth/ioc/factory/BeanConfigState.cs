using System;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc.Factory
{
    public class BeanConfigState
    {
        private readonly IBeanConfiguration beanConfiguration;

        private readonly Type beanType;

        public BeanConfigState(IBeanConfiguration beanConfiguration, Type beanType)
        {
            this.beanConfiguration = beanConfiguration;
            this.beanType = beanType;
        }

        public IBeanConfiguration GetBeanConfiguration()
        {
            return beanConfiguration;
        }

        public Type GetBeanType()
        {
            return beanType;
        }
    }
}