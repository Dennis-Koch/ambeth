
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Security.Transfer
{
    [DataContract(Name = "AmbethServiceException", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class AmbethServiceException
    {
        [DataMember(IsRequired = false)]
	    public String Message { get; set; }

        [DataMember(IsRequired = false)]
        public String StackTrace { get; set; }

        [DataMember(IsRequired = false)]
        public AmbethServiceException Cause { get; set; }
    }
}
