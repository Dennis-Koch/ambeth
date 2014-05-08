using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Merge.Model
{
    [XmlType(Name = "IRUI")]
    public interface IRelationUpdateItem : IUpdateItem
    {
        IObjRef[] AddedORIs { get; }

        IObjRef[] RemovedORIs { get; }
    }
}
