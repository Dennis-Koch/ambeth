using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Progress.Model
{
    public interface IProgressHandle
    {
        long ProgressID { get; }
    }
}