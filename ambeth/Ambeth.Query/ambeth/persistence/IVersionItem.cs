using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IVersionItem : IDisposable
    {
        Object GetId();

        Object GetId(sbyte idIndex);

        Object GetVersion();

        sbyte GetAlternateIdCount();
    }
}