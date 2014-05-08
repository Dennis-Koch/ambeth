using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Cache.Transfer
{
    //[DataContract(Name = "RefId", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class RefId
    {
        [DataMember]
        public int Id { get; set; }

        [DataMember]
        public Object Obj { get; set; }
    }
}
