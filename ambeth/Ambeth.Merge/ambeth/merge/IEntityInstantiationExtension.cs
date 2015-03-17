using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IEntityInstantiationExtension
    {
        Type GetMappedEntityType(Type type);
    }
}