using De.Osthus.Ambeth.Merge.Model;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Objrefstore
{
    public abstract class ObjRefStore : IObjRef
    {
        public static readonly int UNDEFINED_USAGE = -1;

        public ObjRefStore NextEntry { get; set; }

        public int UsageCount { get; set; }

        public virtual bool IsEqualTo(Type entityType, sbyte idIndex, Object id)
        {
            return Id.Equals(id) && RealType.Equals(entityType) && IdNameIndex == idIndex;
        }

        public void IncUsageCount()
        {
            this.UsageCount++;
        }

        public void DecUsageCount()
        {
            this.UsageCount--;
        }

        public abstract sbyte IdNameIndex { get; set; }

        public abstract Object Id { get; set; }

        public abstract Object Version { get; set; }

        public abstract Type RealType { get; set; }

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append("ObjRef").Append(" id=").Append(IdNameIndex).Append(",").Append(Id).Append(" version=").Append(Version).Append(" type=")
                .Append(RealType.FullName);
        }
    }
}