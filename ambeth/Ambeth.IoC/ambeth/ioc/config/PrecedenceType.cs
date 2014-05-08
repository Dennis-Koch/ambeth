using System;
using System.Net;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Config
{
    /// <summary>
    /// Use this value together with a bean configuration
    /// This will stall the initialization of this bean till all other beans (with their default precedence)
    /// will be initialized. The initialization order of beans with the same precedence still remains undefined
    /// </summary>
    public enum PrecedenceType
    {
        DEFAULT,
        LOWEST,
        LOWER,
        LOW,
        MEDIUM,
        HIGH,
        HIGHER,
        HIGHEST
    }
}
