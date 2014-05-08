using System;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IEntityEquals
    {
        Object Get__Id();

        Type Get__BaseType();

        bool Equals(Object obj);

        int GetHashCode();

        String ToString();
    }
}
