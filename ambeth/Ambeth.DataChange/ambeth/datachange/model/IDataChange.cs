using System;
using System.Net;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Datachange.Model
{
    [XmlType(Name = "IDataChange", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IDataChange
    {
        DateTime ChangeTime { get; }

        IList<IDataChangeEntry> All { get; }

        IList<IDataChangeEntry> Deletes { get; }

        IList<IDataChangeEntry> Updates { get; }

        IList<IDataChangeEntry> Inserts { get; }

        bool IsEmpty { get; }

        bool IsEmptyByType(Type entityType);

        bool IsLocalSource { get; }

        IDataChange Derive(params Type[] interestedEntityTypes);

        IDataChange DeriveNot(params Type[] uninterestingEntityTypes);

        IDataChange Derive(params Object[] interestedEntityIds);
    }
}
