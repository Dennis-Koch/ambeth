using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Test.Model;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class EntityMetaDataProviderMock : IEntityMetaDataProvider
    {
        public IEntityMetaData GetMetaData(Type entityType)
        {
            return GetMetaData(entityType, false);
        }

        public IEntityMetaData GetMetaData(Type entityType, bool tryOnly)
        {
            EntityMetaData metadata = null;
            if (typeof(Material).Equals(entityType))
            {
                metadata = new EntityMetaData();
                metadata.EntityType = entityType;
            }
            else if (typeof(Material).Equals(entityType))
            {
                metadata = new EntityMetaData();
                metadata.EntityType = entityType;
            }
            else if (!tryOnly)
            {
                throw new Exception("No metadata found for type '" + entityType.Name);
            }
            return metadata;
        }

        public IList<Type> FindMappableEntityTypes()
        {
            throw new NotImplementedException();
        }

        public IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            throw new NotImplementedException();
        }

        public IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            throw new NotImplementedException();
        }

        public IValueObjectConfig GetValueObjectConfig(String xmlTypeName)
        {
            throw new NotImplementedException();
        }

        public IList<Type> GetValueObjectTypesByEntityType(Type entityType)
        {
            throw new NotImplementedException();
        }

        public Type[] GetEntityPersistOrder()
        {
            throw new NotImplementedException();
        }
    }
}
