using System;
using De.Osthus.Ambeth.Annotation;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Filter.Model
{
    [XmlType(Name = "IFilterDescriptor", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IFilterDescriptor
    {
        String Member { get; }

        IList<String> Value { get; }

        bool? IsCaseSensitive { get; }

        FilterOperator Operator { get; }

        LogicalOperator LogicalOperator { get; }

        IList<IFilterDescriptor> ChildFilterDescriptors { get; }
    }
}