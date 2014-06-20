using System;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class EntityFactoryMock : IEntityFactory
    {
        public object CreateEntity(Type entityType)
        {
            return Activator.CreateInstance(entityType);
        }

        public T CreateEntity<T>()
        {
            return (T)CreateEntity(typeof(T));
        }

        public Object CreateEntity(IEntityMetaData metadata)
        {
            return CreateEntity(metadata.EntityType);
        }

        public bool SupportsEnhancement(Type enhancementType)
        {
            return false;
        }
    }
}
