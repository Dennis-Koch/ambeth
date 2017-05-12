using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Progress.Model;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Progress
{
    [DataContract(Name = "ProgressEvent", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class ProgressEvent : IProgress, IIncrementalProgress, IMessageProgress, IFailureProgress
    {
        [DataMember]
        public long ProgressID { get; set; }

        [DataMember]
        public int MaxSteps { get; set; }

        [DataMember]
        public int CurrentSteps { get; set; }

        [DataMember]
        public String Message { get; set; }

        [DataMember]
        public Object Result { get; set; }

        [DataMember]
        public Exception Exception { get; set; }
    }
}