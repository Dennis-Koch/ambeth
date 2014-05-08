using System;
using System.Net;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Datachange.Model;
using System.Text;

namespace De.Osthus.Ambeth.Datachange.Transfer
{
    [DataContract(Name = "DataChangeEntry", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class DataChangeEntry : IDataChangeEntry, IPrintable
    {
        [DataMember]
        public Type EntityType { get; set; }

        [DataMember]
        public Object Id { get; set; }

        [DataMember]
        public sbyte IdNameIndex { get; set; }

        [DataMember]
        public Object Version { get; set; }

        [DataMember]
        public String[] Topics { get; set; }

        public DataChangeEntry()
        {
            // Intended blank
        }

        public DataChangeEntry(Type entityType, sbyte idNameIndex, Object id, Object version)
        {
            EntityType = entityType;
            IdNameIndex = idNameIndex;
            Id = id;
            Version = version;
        }

        public override String ToString()
	    {
		    StringBuilder sb = new StringBuilder();
		    ToString(sb);
		    return sb.ToString();
	    }

	    public void ToString(StringBuilder sb)
	    {
		    sb.Append("EntityType=").Append(EntityType.FullName).Append(" IdIndex=").Append(IdNameIndex).Append(" Id='").Append(Id)
				    .Append("' Version='").Append(Version).Append('\'');
	    }
    }
}
