using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Cache.Model
{
    [XmlType(Name = "ILoadContainer", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface ILoadContainer
    {
        IObjRef Reference { get; }

        Object[] Primitives { get; set; }

        IObjRef[][] Relations { get; set; }
    }
}
