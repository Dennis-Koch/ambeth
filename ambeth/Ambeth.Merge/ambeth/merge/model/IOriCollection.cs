﻿using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Merge.Model
{
    [XmlType]
    public interface IOriCollection
    {
        IList<IObjRef> AllChangeORIs { get; }

        IList<IObjRef> GetChangeRefs(Type type);

        long? ChangedOn { get; }

        String ChangedBy { get; }

        long[] AllChangedOn { get; }

        String[] AllChangedBy { get; }
    }
}
