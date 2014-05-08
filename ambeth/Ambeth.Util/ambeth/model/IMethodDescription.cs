using System;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Model
{
    [XmlType]
    public interface IMethodDescription
    {
        MethodInfo Method { get; }
    }
}
