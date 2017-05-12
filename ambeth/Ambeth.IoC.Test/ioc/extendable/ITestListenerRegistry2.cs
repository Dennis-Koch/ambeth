using System;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public interface ITestListenerRegistry2
    {
        ITestListener GetTestListener(Type type);
    }
}
