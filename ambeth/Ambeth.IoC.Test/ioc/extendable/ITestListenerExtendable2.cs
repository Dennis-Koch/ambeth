using System;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public interface ITestListenerExtendable2
    {
        void RegisterTestListener(ITestListener testListener, Type type);

        void UnregisterTestListener(ITestListener testListener, Type type);
    }
}