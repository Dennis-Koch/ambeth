using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using System.Text;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "RelationUpdateItem", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class RelationUpdateItem : IRelationUpdateItem, IPrintable
    {
        [DataMember]
        public String MemberName { get; set; }

        [DataMember]
        public IObjRef[] AddedORIs { get; set; }

        [DataMember]
        public IObjRef[] RemovedORIs { get; set; }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append("PUI: MemberName=").Append(MemberName);
            IObjRef[] addedORIs = AddedORIs;
            IObjRef[] removedORIs = RemovedORIs;
            if (addedORIs != null && addedORIs.Length > 0)
		    {
			    sb.Append(" AddedORIs=");
			    Arrays.ToString(sb, addedORIs);
		    }
            if (removedORIs != null && removedORIs.Length > 0)
		    {
			    sb.Append(" RemovedORIs=");
			    Arrays.ToString(sb, removedORIs);
		    }
        }
    }
}
