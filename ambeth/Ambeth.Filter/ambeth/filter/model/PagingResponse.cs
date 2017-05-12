using System;
using De.Osthus.Ambeth.Annotation;
using System.Runtime.Serialization;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Filter.Model
{
    [DataContract(Name = "PagingResponse", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class PagingResponse : IPagingResponse
    {
        [DataMember]
        public int Number { get; set; }

        [DataMember]
        public int TotalNumber { get; set; }

        [IgnoreDataMember]
        public int Size
        {
            get
            {
                IList<IObjRef> refResult = RefResult;
                if (refResult != null)
                {
                    return refResult.Count;
                }
                IList<Object> result = Result;
                if (result != null)
                {
                    return result.Count;
                }
                return 0;
            }
        }

        [DataMember]
        public int TotalSize { get; set; }

        [DataMember]
        public IList<Object> Result { get; set; }

        [DataMember]
        public IList<IObjRef> RefResult { get; set; }

        public PagingResponse WithNumber(int number)
        {
            Number = number;
            return this;
        }

        public PagingResponse WithTotalNumber(int totalNumber)
        {
            TotalNumber = totalNumber;
            return this;
        }

        public PagingResponse WithTotalSize(int totalSize)
        {
            TotalSize = totalSize;
            return this;
        }

        public PagingResponse WithResult(IList<Object> result)
        {
            Result = result;
            return this;
        }

        public PagingResponse WithRefResult(IList<IObjRef> refResult)
        {
            RefResult = refResult;
            return this;
        }
    }
}