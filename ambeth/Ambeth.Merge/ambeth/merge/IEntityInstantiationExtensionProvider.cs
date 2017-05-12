using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IEntityInstantiationExtensionProvider
    {
        IEntityInstantiationExtension GetEntityInstantiationExtension(Type entityType);
    }
}
