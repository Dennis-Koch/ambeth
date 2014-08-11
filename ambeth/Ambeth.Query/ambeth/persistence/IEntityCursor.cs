using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IEntityCursor<T> : IDisposable, IEnumerator<T>
    {
        new bool MoveNext();

        new T Current { get; }
    }
}