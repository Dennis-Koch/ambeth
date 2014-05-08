using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Progress.Model
{
    public interface IIncrementalProgress : IProgress
    {
        int MaxSteps { get; }

        int CurrentSteps { get; }
    }
}