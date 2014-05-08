using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Progress.Model
{
    public interface IMessageProgress : IProgress
    {
        String Message { get; }
    }
}