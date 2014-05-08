using System;
using De.Osthus.Ambeth.Annotation;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Filter.Model
{
    [XmlType(Name = "IPagingResponse", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IPagingResponse
    {
        int Size { get; }

        int TotalSize { get; }

        int Number { get; }

        int TotalNumber { get; }

        IList<IObjRef> RefResult { get; set; }

        IList<Object> Result { get; set; }
    }
}