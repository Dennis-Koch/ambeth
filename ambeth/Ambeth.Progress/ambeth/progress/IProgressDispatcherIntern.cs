using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Progress.Model;

namespace De.Osthus.Ambeth.Progress
{
    public interface IProgressDispatcherIntern : IProgressDispatcher
    {
        void Failure(Exception e);

        void EndProgress(Object progressResult);
    }
}