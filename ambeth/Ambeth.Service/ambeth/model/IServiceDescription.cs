using System;
using System.Reflection;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Model
{
    [XmlType(Name = "IServiceDescription", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IServiceDescription 
    {
        String ServiceName { get; }

        MethodInfo GetMethod(Type serviceType);

        Object[] Arguments { get; }

        ISecurityScope[] SecurityScopes { get; }
    }
}
