using System;

namespace De.Osthus.Ambeth.Cache
{
    public interface IEntityEnhancer
    {
        bool IsEnhancedType(Type entityType);

        Type GetBaseType(Type enhancedType);

        Type EnhanceEntityType(Type entityType);

        Type EnhanceEmbeddedType(Type embeddedType);

        bool SupportsEnhancement(Type enhancementType);
    }
}