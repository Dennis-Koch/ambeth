using System;

namespace De.Osthus.Ambeth.Ioc.Config
{

    public interface IPropertyConfiguration
    {
        String GetDeclarationStackTrace();

        IBeanConfiguration BeanConfiguration { get; }

        String GetPropertyName();

        String GetBeanName();

        bool IsOptional();

        Object GetValue();
    }
}
