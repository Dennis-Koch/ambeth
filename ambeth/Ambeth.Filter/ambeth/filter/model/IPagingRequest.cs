using System;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Filter.Model
{
    [XmlType(Name = "IPagingRequest", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IPagingRequest
    {
        int Number { get; }

        int Size { get; }
    }
}