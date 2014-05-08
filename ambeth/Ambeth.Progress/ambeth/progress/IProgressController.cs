using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Progress.Model;

namespace De.Osthus.Ambeth.Progress
{
    public interface IProgressController : IProgressDispatcher
    {
        IProgress StartProgress();

        void EndProgress();

        void EndProgress(Object result);

        void Failure(Exception e);
    }
}