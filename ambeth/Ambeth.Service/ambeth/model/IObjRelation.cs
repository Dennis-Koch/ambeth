using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Cache.Model
{
    [XmlType(Name = "IObjRelation", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IObjRelation : IPrintable
    {
        String MemberName { get; }

        IObjRef[] ObjRefs { get; }

        Type RealType { get; }

        Object Version { get; }
    }
}