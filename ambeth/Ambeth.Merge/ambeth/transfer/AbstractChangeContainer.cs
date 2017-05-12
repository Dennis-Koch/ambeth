using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using System.Text;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "AbstractChangeContainer", Namespace = "http://schemas.osthus.de/Ambeth")]
    abstract public class AbstractChangeContainer : IChangeContainer, IPrintable
    {
        [DataMember]
        public IObjRef Reference { get; set; }

        public override String ToString()
	    {
		    StringBuilder sb = new StringBuilder();
		    ToString(sb);
		    return sb.ToString();
	    }

	    public virtual void ToString(StringBuilder sb)
	    {
		    sb.Append(GetType().Name).Append(": ");
		    StringBuilderUtil.AppendPrintable(sb, Reference);
	    }
    }
}
