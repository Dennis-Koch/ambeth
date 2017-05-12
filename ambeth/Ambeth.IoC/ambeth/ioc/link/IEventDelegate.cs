using System;
using System.Net;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface IEventDelegate<T> : IEventDelegate
    {
        // Intended blank
    }

    public interface IEventDelegate
    {
        String EventName { get; }
    }
}
