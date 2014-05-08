using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IEntityFactoryExtensionExtendable
    {
        void RegisterEntityFactoryExtension(IEntityFactoryExtension entityFactoryExtension, Type type);

        void UnregisterEntityFactoryExtension(IEntityFactoryExtension entityFactoryExtension, Type type);
    }
}