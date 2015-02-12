using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IVersionCursor : IDisposable, IEnumerator<IVersionItem>
    {
        new bool MoveNext();

        new IVersionItem Current { get; }

        int GetAlternateIdCount();
    }
}