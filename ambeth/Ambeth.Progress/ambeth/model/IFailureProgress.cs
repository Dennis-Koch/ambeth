using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Progress.Model
{
    public interface IFailureProgress : IProgress
    {
        Exception Exception { get; }
    }
}