using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Merge.Model
{
    [XmlType(Name = "IObjRef", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IObjRef : IPrintable
    {
        sbyte IdNameIndex { get; set; }

        Object Id { get; set; }

        Object Version { get; set; }
        
        Type RealType { get; set; }
    }
}
