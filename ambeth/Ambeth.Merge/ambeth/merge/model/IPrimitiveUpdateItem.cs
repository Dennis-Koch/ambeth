using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Merge.Model
{
    [XmlType(Name = "IPUI")]
    public interface IPrimitiveUpdateItem : IUpdateItem
    {
        Object NewValue { get; }
    }
}
