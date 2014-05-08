using System;
using System.ComponentModel;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Helloworld.Transfer
{
    [DataContract(Namespace = "HelloWorld")]
    public class EmbeddedObject
    {
        [DataMember]
        public virtual String Name { get; set; }

        [DataMember]
        public virtual int Value { get; set; }
    }
}
