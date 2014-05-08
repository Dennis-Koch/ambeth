using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Merge.Model;
using System.Text;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Merge.Transfer
{
    [DataContract(Name = "UpdateContainer", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class UpdateContainer : AbstractChangeContainer
    {
        [DataMember]
        public IPrimitiveUpdateItem[] Primitives { get; set; }

        [DataMember]
        public IRelationUpdateItem[] Relations { get; set; }

        public override void ToString(StringBuilder sb)
        {
            base.ToString(sb);
            sb.Append(" Primitives=");
            Arrays.ToString(sb, Primitives);
            sb.Append(" Relations=");
            Arrays.ToString(sb, Relations);
        }
    }
}
