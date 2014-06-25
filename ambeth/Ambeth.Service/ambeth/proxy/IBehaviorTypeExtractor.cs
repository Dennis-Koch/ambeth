using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IBehaviourTypeExtractor<A, T> where A : Attribute
    {
        T ExtractBehaviourType(A annotation);
    }
}