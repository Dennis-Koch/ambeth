using System;
using System.Text;
using System.Text.RegularExpressions;

namespace De.Osthus.Ambeth.Metadata
{
    public sealed class EmbeddedMember
    {
        private static readonly Regex pattern = new Regex("\\.");

	    public static String[] Split(String memberName)
	    {
		    return pattern.Split(memberName);
	    }

        public static String BuildMemberPathString(Member[] memberPath)
        {
            StringBuilder sb = new StringBuilder();
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                Member member = memberPath[a];
                if (a > 0)
                {
                    sb.Append('.');
                }
                sb.Append(member.Name);
            }
            return sb.ToString();
        }

        public static String[] BuildMemberPathToken(Member[] memberPath)
        {
            String[] token = new String[memberPath.Length];
            for (int a = memberPath.Length; a-- > 0; )
            {
                Member member = memberPath[a];
                token[a] = member.Name;
            }
            return token;
        }

        private EmbeddedMember()
        {
            // intended blank
        }
    }
}