using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc
{
    public interface IPropertyLoadingBean
    {
        void ApplyProperties(Properties contextProperties);
    }
}
