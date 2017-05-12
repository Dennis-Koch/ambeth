namespace De.Osthus.Ambeth.Cache.Mock
{
    /**
     * Support for unit tests that do not include jAmbeth.Cache
     */
    public class CacheProviderMock : ICacheProvider
    {
        public ICache GetCurrentCache()
        {
            return null;
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