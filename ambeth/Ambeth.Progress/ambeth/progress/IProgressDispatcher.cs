using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Progress.Model;

namespace De.Osthus.Ambeth.Progress
{
    public interface IProgressDispatcher
    {
        bool IsProgressPending { get; }
        
        void Step();

        void Step(int stepCount);

        void Step(int stepCount, int maxCount);
    }
}