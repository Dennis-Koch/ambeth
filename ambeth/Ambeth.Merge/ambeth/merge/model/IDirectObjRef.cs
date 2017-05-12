using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Merge.Model
{
    [XmlType]
    public interface IDirectObjRef : IObjRef
    {
        Object Direct { get; set; }

        int CreateContainerIndex { get; set; }
    }
}
