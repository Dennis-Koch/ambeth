using System;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Datachange.Model
{
    [XmlType(Name = "IDataChangeEntry", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IDataChangeEntry
    {
        Type EntityType { get; }

        sbyte IdNameIndex { get; }

        Object Id { get; }

        Object Version { get; }

        String[] Topics { get; set; }
    }
}
