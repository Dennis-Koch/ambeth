using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IDataItem : IDisposable
    {
        Object GetValue(String propertyName);

        Object GetValue(int index);

        int GetFieldCount();
    }
}