namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheProvider
    {
        ICache GetCurrentCache();

        bool IsNewInstanceOnCall { get; }
    }
}