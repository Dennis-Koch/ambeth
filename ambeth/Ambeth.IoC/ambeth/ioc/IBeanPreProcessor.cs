using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc
{
    public interface IBeanPreProcessor
    {
        void PreProcessProperties(IBeanContextFactory beanContextFactory, IProperties props, String beanName, Object service, Type beanType, IList<IPropertyConfiguration> propertyConfigs, IPropertyInfo[] properties);
    }
}
