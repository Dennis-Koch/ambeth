namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public interface IThreadLocalCleanupBeanExtendable
    {
        void RegisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean);

        void UnregisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean);
    }
}