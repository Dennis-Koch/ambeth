using System;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Orm
{
    public class MemberConfig : AbstractMemberConfig
    {
        public String ColumnName { get; set; }

        public MemberConfig(String name)
            : this(name, null)
        { }

        public MemberConfig(String name, String columnName)
            : base(name)
        {
            this.ColumnName = columnName;
        }

        public override bool Equals(Object obj)
        {
            if (obj is MemberConfig)
            {
                MemberConfig other = (MemberConfig)obj;
                return Name.Equals(other.Name);
            }
            else
            {
                return false;
            }
        }

        public override int GetHashCode()
        {
            return GetType().GetHashCode() ^ Name.GetHashCode();
        }
    }
}