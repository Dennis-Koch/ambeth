using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IBehaviorTypeExtractor<A, T> where A : Attribute
    {
        T ExtractBehaviorType(A annotation);
    }
}