
namespace De.Osthus.Ambeth.Ioc
{
    public interface IServiceContextIntern : IServiceContext
    {
        void ChildContextDisposed(IServiceContext childContext);
    }
}
