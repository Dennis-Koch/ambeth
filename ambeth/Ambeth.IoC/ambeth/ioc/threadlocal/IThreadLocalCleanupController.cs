namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public interface IThreadLocalCleanupController
    {
        void CleanupThreadLocal();

        IForkState CreateForkState();
    }
}