using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IVersionItem
    {
        Object GetId();

        Object GetId(int idIndex);

        Object GetVersion();

        int GetAlternateIdCount();
    }
}