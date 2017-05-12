using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IDataCursor : IDisposable, IEnumerator<IDataItem>
    {
        new bool MoveNext();

        new IDataItem Current { get; }

        int GetFieldCount();
    }
}