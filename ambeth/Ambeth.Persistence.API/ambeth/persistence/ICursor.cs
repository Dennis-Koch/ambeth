using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ICursor : IDisposable, IEnumerator<ICursorItem>
    {
        IField[] GetFields();

        IField GetFieldByMemberName(String memberName);

        int GetFieldIndexByMemberName(String memberName);

        int GetFieldIndexByName(String fieldName);

        new bool MoveNext();

        new ICursorItem Current { get; }
    }
}