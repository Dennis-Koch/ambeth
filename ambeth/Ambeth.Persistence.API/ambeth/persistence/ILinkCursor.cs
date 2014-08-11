using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ILinkCursor : IDisposable, IEnumerator<ILinkCursorItem>
    {
        new bool MoveNext();

        new ILinkCursorItem Current { get; }

        sbyte GetFromIdIndex();

        sbyte GetToIdIndex();
    }
}