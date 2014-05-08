using System;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Ambeth.Ioc
{

    public interface IBeanRuntime<V>
    {
        V Finish();

        IBeanRuntime<V> Parent(String parentBeanTemplateName);

        IBeanRuntime<V> PropertyRef(String propertyName, String beanName);

        IBeanRuntime<V> PropertyRef(String propertyName, IBeanConfiguration bean);

        IBeanRuntime<V> PropertyRefs(String beanName);

        IBeanRuntime<V> PropertyRefs(String[] beanNames);

        IBeanRuntime<V> PropertyRef(IBeanConfiguration bean);

        IBeanRuntime<V> PropertyValue(String propertyName, Object value);

        IBeanRuntime<V> IgnoreProperties(String propertyName);

        IBeanRuntime<V> IgnoreProperties(params String[] propertyNames);
    }
}
