using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Event.Model
{
    [XmlType(Name = "IEventItem", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface IEventItem
	{
        Object EventObject { get; }

        long SequenceNumber { get; }

        DateTime DispatchTime { get; }
	}
}
