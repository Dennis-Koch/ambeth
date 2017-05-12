using System;

namespace De.Osthus.Ambeth.Garbageproxy
{
    public interface IGarbageProxyFactory
    {
        IGarbageProxyConstructor<T> CreateGarbageProxyConstructor<T>(params Type[] additionalInterfaceTypes);

        T CreateGarbageProxy<T>(IDisposable target, params Type[] additionalInterfaceTypes);

        T CreateGarbageProxy<T>(Object target, IDisposable disposable, params Type[] additionalInterfaceTypes);
    }
}