using System;
using System.Net;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public class CacheUnmodifiedObjectProvider : IUnmodifiedObjectProvider, IInitializingBean
    {
        public ICache Cache { get; set; }

        public IEntityMetaDataProvider EntityMetaDataProvider { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(Cache, "Cache");
            ParamChecker.AssertNotNull(EntityMetaDataProvider, "EntityMetaDataProvider");
        }

        public virtual Object GetUnmodifiedObject(Type type, Object id)
        {
            return Cache.GetObject(type, id);
        }

        public virtual Object GetUnmodifiedObject(Object modifiedObject)
        {
            if (modifiedObject == null)
            {
                return null;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(modifiedObject.GetType());
            Object id = metaData.IdMember.GetValue(modifiedObject, false);
            return GetUnmodifiedObject(metaData.EntityType, id);
        }
    }
}
