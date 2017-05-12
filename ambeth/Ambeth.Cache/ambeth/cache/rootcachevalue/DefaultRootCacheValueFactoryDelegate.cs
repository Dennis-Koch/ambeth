using De.Osthus.Ambeth.Merge.Model;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public class DefaultRootCacheValueFactoryDelegate : RootCacheValueFactoryDelegate
    {
        protected readonly ConstructorInfo constructor;

        public DefaultRootCacheValueFactoryDelegate()
        {
            ConstructorInfo ci = typeof(DefaultRootCacheValue).GetConstructor(new Type[] { typeof(IEntityMetaData) });
            constructor = typeof(DefaultRootCacheValue).GetConstructor(new Type[] { typeof(IEntityMetaData) });
        }

        public override RootCacheValue CreateRootCacheValue(IEntityMetaData metaData)
        {
            return (RootCacheValue)constructor.Invoke(new Object[] { metaData });
        }
    }
}