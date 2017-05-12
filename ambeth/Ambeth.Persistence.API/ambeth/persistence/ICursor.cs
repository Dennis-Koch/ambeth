using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ICursor : IDisposable, IEnumerator<ICursorItem>
    {
        IFieldMetaData[] GetFields();

        IFieldMetaData GetFieldByMemberName(String memberName);

        int GetFieldIndexByMemberName(String memberName);

        int GetFieldIndexByName(String fieldName);

        new bool MoveNext();

        new ICursorItem Current { get; }
    }
}