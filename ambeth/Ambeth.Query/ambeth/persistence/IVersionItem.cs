using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IVersionItem : IDisposable
    {
        Object GetId();

        Object GetId(int idIndex);

        Object GetVersion();

        int GetAlternateIdCount();
    }
}