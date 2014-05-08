using System;
using De.Osthus.Ambeth.Annotation;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Filter.Model
{
    [DataContract(Name = "PagingRequest", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class PagingRequest : IPagingRequest
    {
        [DataMember]
        public int Number { get; set; }

        [DataMember]
        public int Size { get; set; }

        public PagingRequest WithNumber(int number)
        {
            Number = number;
            return this;
        }

        public PagingRequest WithSize(int size)
        {
            Size = size;
            return this;
        }
    }
}