using System;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Filter.Model
{
    [XmlType(Name = "ISortDescriptor", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface ISortDescriptor
    {
        String Member { get; }

        SortDirection SortDirection { get; }
    }
}