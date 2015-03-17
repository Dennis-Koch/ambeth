using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IEntityInstantiationExtensionExtendable
    {
        void RegisterEntityInstantiationExtension(IEntityInstantiationExtension entityInstantiationExtension, Type type);

        void UnregisterEntityInstantiationExtension(IEntityInstantiationExtension entityInstantiationExtension, Type type);
    }
}