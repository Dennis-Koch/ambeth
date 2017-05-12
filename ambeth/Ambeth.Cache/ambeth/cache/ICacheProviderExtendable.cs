namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheProviderExtendable
    {
        void RegisterCacheProvider(ICacheProvider cacheProvider);

        void UnregisterCacheProvider(ICacheProvider cacheProvider);
    }
}