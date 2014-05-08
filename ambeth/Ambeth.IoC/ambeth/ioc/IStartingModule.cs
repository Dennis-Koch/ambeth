namespace De.Osthus.Ambeth.Ioc
{

    public interface IStartingModule
    {

        void AfterStarted(IServiceContext serviceContext);

    }
}
