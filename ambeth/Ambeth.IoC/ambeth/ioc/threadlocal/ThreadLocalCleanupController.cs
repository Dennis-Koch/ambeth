using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ThreadLocalCleanupController : IInitializingBean, IThreadLocalCleanupBeanExtendable, IThreadLocalCleanupController
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly IExtendableContainer<IThreadLocalCleanupBean> listeners = new DefaultExtendableContainer<IThreadLocalCleanupBean>("threadLocalCleanupBean");

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public virtual void CleanupThreadLocal()
        {
            IThreadLocalCleanupBean[] extensions = listeners.GetExtensions();
            for (int a = 0, size = extensions.Length; a < size; a++)
            {
                extensions[a].CleanupThreadLocal();
            }
        }

        public void RegisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
        {
            listeners.Register(threadLocalCleanupBean);
        }

        public void UnregisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean)
        {
            listeners.Unregister(threadLocalCleanupBean);
        }
    }
}
