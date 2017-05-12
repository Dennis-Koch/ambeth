using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Progress.Model
{
    public interface IChunkProgress : IProgress
    {
        Object Chunk { get; }
    }
}