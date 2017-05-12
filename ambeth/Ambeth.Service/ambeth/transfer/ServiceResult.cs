using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Cache.Transfer
{
    [DataContract(Name = "ServiceResult", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class ServiceResult : IServiceResult
    {
        [DataMember]
        public IList<IObjRef> ObjRefs { get; set; }

        [DataMember]
        public Object AdditionalInformation { get; set; }

        [IgnoreDataMember]
        public Object HardRefs { get; set; }
    }
}
