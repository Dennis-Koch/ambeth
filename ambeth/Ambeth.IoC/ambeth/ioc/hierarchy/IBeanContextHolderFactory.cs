using System;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public interface IBeanContextHolderFactory<V>
    {
        IBeanContextHolder<V> Create();

        IBeanContextHolder<V> Create(Object[] autowireableSourceBeans);
    }
}
