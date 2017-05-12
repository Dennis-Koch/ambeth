using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Merge
{
    /// <summary>
    /// Creates (enhanced) instances of classes and interfaces.
    /// </summary>
    public interface IEntityFactory
    {
        bool SupportsEnhancement(Type enhancementType);

        T CreateEntity<T>();

        Object CreateEntity(Type entityType);

        Object CreateEntity(IEntityMetaData metadata);
    }
}