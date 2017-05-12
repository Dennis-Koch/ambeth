
namespace De.Osthus.Ambeth.Cache.Interceptor
{
    public class SingleCacheProvider : ICacheProvider
    {
        protected ICache cache;

        public SingleCacheProvider(ICache cache)
        {
            this.cache = cache;
        }

        public ICache GetCurrentCache()
        {
            return cache;
        }

        public bool IsNewInstanceOnCall
        {
            get
            {
                return false;
            }
        }
    }
}