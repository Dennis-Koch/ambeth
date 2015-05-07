using System;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface IRelationProvider
    {
        bool IsEntityType(Type type);
    }
}
