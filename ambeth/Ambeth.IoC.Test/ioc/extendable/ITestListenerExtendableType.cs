using System;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public interface ITestListenerExtendableType
    {
        void AddTestListener(ITestListener testListener, Type type);

        void RemoveTestListener(ITestListener testListener, Type type);

    }
}