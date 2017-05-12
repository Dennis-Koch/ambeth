using System;
using System.Diagnostics;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public interface IPropertyConfiguration
    {
        StackFrame[] GetDeclarationStackTrace();

        IBeanConfiguration BeanConfiguration { get; }

        String GetPropertyName();

        String GetBeanName();

        bool IsOptional();

        Object GetValue();
    }
}
