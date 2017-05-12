using System;
using System.Net;
using System.Runtime.Serialization;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Datachange.Model
{
    [XmlType(Name = "ILoadedDataChangeEntry", Namespace = "http://schemas.osthus.de/Ambeth")]
    public interface ILoadedDataChangeEntry : IDataChangeEntry
    {
        Object Content { get; }
    }

    //[CollectionDataContract(IsReference = true, Namespace = "http://schemas.osthus.de/Ambeth.DataChange")]
    //public class ListOfILoadedDataChangeEntry : List<ILoadedDataChangeEntry>
    //{
    //    // Intended blank
    //}
}
