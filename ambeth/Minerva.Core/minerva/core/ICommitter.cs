using System.Collections.Generic;
using De.Osthus.Ambeth.Security;
using System;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Minerva.Core
{
    public interface ICommitter<T>
    {
        void Commit(IList<T> objectsToCommit);
    }
}
