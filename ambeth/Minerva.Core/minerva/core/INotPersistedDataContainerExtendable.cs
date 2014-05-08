using System;

namespace De.Osthus.Minerva.Core
{
    public interface INotPersistedDataContainerExtendable
    {
        void RegisterNotPersistedDataContainer(Object npdc);

        void UnregisterNotPersistedDataContainer(Object npdc);
    }
}
