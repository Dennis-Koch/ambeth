using System;
using System.Collections.Generic;

namespace De.Osthus.Minerva.Core
{
    public interface IModelContainerRegistry
    {
        IList<Object> GetModelContainers();
    }
}
