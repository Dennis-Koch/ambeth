
namespace De.Osthus.Ambeth.Cache
{
    public sealed class ClearAllCachesEvent
    {
        protected static readonly ClearAllCachesEvent instance = new ClearAllCachesEvent();

        public static ClearAllCachesEvent getInstance()
        {
            return instance;
        }

        private ClearAllCachesEvent()
        {
            // intended blank
        }
    }
}
