using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Transfer;

namespace De.Osthus.Ambeth.Cache.Transfer
{
    [DataContract(Name = "LoadContainer", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class LoadContainer : ILoadContainer
    {
        [DataMember]
        public IObjRef Reference { get; set; }
        
        [DataMember]
        public Object[] Primitives { get; set; }

        [DataMember]
        public IObjRef[][] Relations { get; set; }
    }
}
