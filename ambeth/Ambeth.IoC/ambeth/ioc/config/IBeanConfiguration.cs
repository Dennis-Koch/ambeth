using System;
using System.Collections.Generic;
using System.Diagnostics;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public interface IBeanConfiguration
    {
        StackFrame[] GetDeclarationStackTrace();

        Object GetInstance();

        Object GetInstance(Type instanceType);

        String GetName();

        String GetParentName();

        Type GetBeanType();

        bool IsAbstract();

        bool IsWithLifecycle();

        bool IsOverridesExisting();

        PrecedenceType GetPrecedence();

        IList<String> GetIgnoredPropertyNames();

        IList<Type> GetAutowireableTypes();

        IList<IPropertyConfiguration> GetPropertyConfigurations();

        IBeanConfiguration Precedence(PrecedenceType precedenceType);

        IBeanConfiguration Template();

        IBeanConfiguration Parent(String parentBeanTemplateName);

        IBeanConfiguration OverridesExisting();

        IBeanConfiguration Autowireable<T>();

        IBeanConfiguration Autowireable(Type typeToPublish);

        IBeanConfiguration Autowireable(params Type[] typesToPublish);

        IBeanConfiguration PropertyRef(String propertyName, String beanName);

        IBeanConfiguration PropertyRef(String propertyName, IBeanConfiguration bean);

        IBeanConfiguration PropertyRefs(String beanName);

        IBeanConfiguration PropertyRefs(params String[] beanNames);

        IBeanConfiguration PropertyRef(IBeanConfiguration bean);

        IBeanConfiguration PropertyRefs(params IBeanConfiguration[] beans);

        IBeanConfiguration PropertyValue(String propertyName, Object value);

        IBeanConfiguration IgnoreProperties(String propertyName);

        IBeanConfiguration IgnoreProperties(params String[] propertyNames);
    }
}
