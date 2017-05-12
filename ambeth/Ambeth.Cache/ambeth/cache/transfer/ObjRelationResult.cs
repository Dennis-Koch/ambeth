using System;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Cache.Transfer
{
    [DataContract(Name = "ObjRelationResult", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class ObjRelationResult : IObjRelationResult
    {
        [DataMember]
        public IObjRelation Reference { get; set; }

        [DataMember]
        public IObjRef[] Relations { get; set; }
    }
}
