using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Progress.Model;

namespace De.Osthus.Ambeth.Progress
{
    public interface IProgressListener
    {
        void HandleProgress(IProgress progress);
    }
}