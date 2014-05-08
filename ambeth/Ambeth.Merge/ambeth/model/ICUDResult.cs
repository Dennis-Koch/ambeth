using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Merge.Model
{
    [XmlType]
    public interface ICUDResult
    {
        IList<IChangeContainer> AllChanges { get; }

        IList<IChangeContainer> GetChanges(Type type);

        IList<Object> GetOriginalRefs();
    }
}
