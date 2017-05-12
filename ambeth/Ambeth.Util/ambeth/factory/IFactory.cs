using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Factory
{
    public interface IFactory<V>
    {
        V Create(params Object[] args);
    }
}
