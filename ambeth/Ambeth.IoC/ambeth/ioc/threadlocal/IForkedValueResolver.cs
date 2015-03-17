using System;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public interface IForkedValueResolver
    {
        Object CreateForkedValue();

        Object GetOriginalValue();
    }
}