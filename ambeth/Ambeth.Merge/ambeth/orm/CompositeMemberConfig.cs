using System;
using System.Text;

namespace De.Osthus.Ambeth.Orm
{
    public class CompositeMemberConfig : AbstractMemberConfig
    {
        private static String ConstructName(MemberConfig[] members)
        {
            StringBuilder sb = new StringBuilder(members[0].Name);
            for (int i = 1; i < members.Length; i++)
            {
                MemberConfig member = members[i];
                sb.Append('-').Append(member.Name);
            }
            return sb.ToString();
        }

        private readonly MemberConfig[] members;

        public CompositeMemberConfig(MemberConfig[] members)
            : base(ConstructName(members))
        {
            this.members = members;
        }

        public MemberConfig[] GetMembers()
        {
            return members;
        }

        public override bool Equals(Object obj)
        {
            if (obj is CompositeMemberConfig)
            {
                return Equals((AbstractMemberConfig)obj);
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