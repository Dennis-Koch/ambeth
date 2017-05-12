using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Cache.Model
{
    [XmlType(Name = "IObjRelationResult", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IObjRelationResult
    {
        IObjRelation Reference { get; }
        
        IObjRef[] Relations { get; }
    }
}