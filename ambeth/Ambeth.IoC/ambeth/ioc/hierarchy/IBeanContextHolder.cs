using System;
using De.Osthus.Ambeth.Ioc.Link;

namespace De.Osthus.Ambeth.Ioc.Hierarchy
{
    public interface IBeanContextHolder<V> : IBeanContextHolder, IDisposable
    {
        V GetTypedValue();
    }

    public interface IBeanContextHolder : IDisposable
    {
        Object GetValue();

        ILinkRuntimeExtendable LinkExtendable { get; }
    }
}
