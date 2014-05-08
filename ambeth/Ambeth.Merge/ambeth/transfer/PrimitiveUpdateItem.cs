using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using System.Text;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "PrimitiveUpdateItem", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class PrimitiveUpdateItem : IPrimitiveUpdateItem, IPrintable
    {
        [DataMember]
        public Object NewValue { get; set; }

        [DataMember]
        public String MemberName { get; set; }

        public PrimitiveUpdateItem()
        {
            // Intended blank
        }

        public PrimitiveUpdateItem(Object newValue, String memberName)
        {
            this.NewValue = newValue;
            this.MemberName = memberName;
        }

        public override String ToString()
	    {
		    StringBuilder sb = new StringBuilder();
		    ToString(sb);
		    return sb.ToString();
	    }

	    public void ToString(StringBuilder sb)
	    {
		    sb.Append("PUI: MemberName=").Append(MemberName).Append(" NewValue='").Append(NewValue).Append('\'');
	    }
    }
}
