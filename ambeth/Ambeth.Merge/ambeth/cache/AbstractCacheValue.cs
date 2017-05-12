using System;
using De.Osthus.Ambeth.Util;
using System.Text;

namespace De.Osthus.Ambeth.Cache
{
    public abstract class AbstractCacheValue : IPrintable
    {
        public abstract Object Id { get; set; }

        public abstract Object Version { get; set; }

        public abstract Type EntityType { get; }

        public abstract Object GetPrimitive(int primitiveIndex);

        public abstract Object[] GetPrimitives();

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append("EntityType=").Append(EntityType.FullName).Append(" Id='").Append(Id).Append("' Version='").Append(Version).Append('\'');
        }
    }
}
