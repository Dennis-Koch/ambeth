
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
namespace De.Osthus.Minerva.Core
{
    public interface ISelfRegisteringControlBean
    {
        void RegisterSelf(IBeanConfiguration beanConfiguration, IServiceContext beanContext, IBeanContextFactory beanContextFactory);
    }
}
