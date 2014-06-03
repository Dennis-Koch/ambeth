using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Merge.Model
{
    [XmlType]
    public interface IUpdateItem
    {
        String MemberName { get; }
    }
}
