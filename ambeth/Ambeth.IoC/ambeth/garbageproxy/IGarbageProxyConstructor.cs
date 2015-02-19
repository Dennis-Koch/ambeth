using System;
namespace De.Osthus.Ambeth.Garbageproxy
{
    public interface IGarbageProxyConstructor<T> : IGarbageProxyConstructor
    {
        new T CreateInstance(IDisposable target);

        new T CreateInstance(Object target, IDisposable disposable);
    }

    public interface IGarbageProxyConstructor
    {
        Object CreateInstance(IDisposable target);

        Object CreateInstance(Object target, IDisposable disposable);
    }
}