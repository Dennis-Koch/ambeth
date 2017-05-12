using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Ioc
{
    public interface IInitializingModule
    {
        void AfterPropertiesSet(IBeanContextFactory beanContextFactory);
    }
}
