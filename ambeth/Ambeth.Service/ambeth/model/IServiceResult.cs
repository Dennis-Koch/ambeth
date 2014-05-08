using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Cache.Model
{
    [XmlType(Name = "IServiceResult", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IServiceResult
    {
        IList<IObjRef> ObjRefs { get; }

        Object AdditionalInformation { get; }
    }
}