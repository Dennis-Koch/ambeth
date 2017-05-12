using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface ITechnicalEntityTypeExtendable
    {
        void RegisterTechnicalEntityType(Type technicalEntityType, Type entityType);

        void UnregisterTechnicalEntityType(Type technicalEntityType, Type entityType);
    }
}