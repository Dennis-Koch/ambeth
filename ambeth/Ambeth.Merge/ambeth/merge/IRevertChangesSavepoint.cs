using System;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface IRevertChangesSavepoint : IDisposable
    {
        void RevertChanges();

        Object[] GetSavedBusinessObjects();
    }
}
