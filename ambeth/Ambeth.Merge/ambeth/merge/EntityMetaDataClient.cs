using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Merge
{
    public class EntityMetaDataClient : IEntityMetaDataProvider
    {
        [Autowired]
        public ICache Cache { protected get; set; }
        
        [Autowired]
        public IMergeService MergeService { protected get; set; }

        [Autowired]
        public IProxyHelper ProxyHelper { protected get; set; }

        public virtual IEntityMetaData GetMetaData(Type entityType)
        {
            return GetMetaData(entityType, false);
        }

        public virtual IEntityMetaData GetMetaData(Type entityType, bool tryOnly)
        {
            IList<IEntityMetaData> metaData = GetMetaData(new List<Type> { entityType });
            if (metaData.Count > 0)
            {
                return metaData[0];
            }
            if (tryOnly)
            {
                return null;
            }
            throw new Exception("No metadata found for entity of type " + entityType.Name);
        }

        public virtual IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            IList<Type> realEntityTypes = new List<Type>(entityTypes.Count);
            foreach (Type entityType in entityTypes)
            {
                realEntityTypes.Add(ProxyHelper.GetRealType(entityType));
            }
            Lock readLock = Cache.ReadLock;
            LockState lockState = readLock.ReleaseAllLocks();
            try
            {
                return MergeService.GetMetaData(realEntityTypes);
            }
            finally
            {
                readLock.ReacquireLocks(lockState);
            }
        }

        public IList<Type> FindMappableEntityTypes()
        {
            throw new NotSupportedException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
        }

        public IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            return MergeService.GetValueObjectConfig(valueType);
        }

        public IValueObjectConfig GetValueObjectConfig(String xmlTypeName)
        {
            throw new NotSupportedException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
        }

        public IList<Type> GetValueObjectTypesByEntityType(Type entityType)
        {
            throw new NotSupportedException("This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
        }

        public Type[] GetEntityPersistOrder()
        {
            return Type.EmptyTypes;
        }
    }
}
