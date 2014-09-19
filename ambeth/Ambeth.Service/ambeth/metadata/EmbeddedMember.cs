using System;
using System.Text;

namespace De.Osthus.Ambeth.Metadata
{
    public class EmbeddedMember : Member, IEmbeddedMember
    {
        private readonly Member childMember;

        private readonly Member[] memberPath;

        private readonly String name;

        public EmbeddedMember(String name, Member childMember, Member[] memberPath)
            : base(null, null)
        {
            this.name = name;
            this.childMember = childMember;
            this.memberPath = memberPath;
        }

        public Member[] GetMemberPath()
        {
            return memberPath;
        }

        public String GetMemberPathString()
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

        public override Object NullEquivalentValue
        {
            get
            {
                return childMember.NullEquivalentValue;
            }
        }

        public String[] GetMemberPathToken()
        {
            Member[] memberPath = GetMemberPath();
            String[] token = new String[memberPath.Length];
            for (int a = memberPath.Length; a-- > 0; )
            {
                Member member = memberPath[a];
                token[a] = member.Name;
            }
            return token;
        }

        public Member ChildMember
        {
            get
            {
                return childMember;
            }
        }

        public override Type DeclaringType
        {
            get
            {
                return memberPath[0].DeclaringType;
            }
        }

        public override Type ElementType
        {
            get
            {
                return childMember.ElementType;
            }
        }

        public override Type RealType
        {
            get
            {
                return childMember.RealType;
            }
        }

        public V GetAnnotation<V>() where V : Attribute
        {
            return childMember.GetAnnotation<V>();
        }

        public override String Name
        {
            get
            {
                return name;
            }
        }

        public override bool CanRead
        {
            get
            {
                return childMember.CanRead;
            }
        }

        public override bool CanWrite
        {
            get
            {
                return childMember.CanWrite;
            }
        }

        public override Object GetValue(Object obj)
        {
            Object currentObj = obj;
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                Member memberPathItem = memberPath[a];
                currentObj = memberPathItem.GetValue(currentObj, false);
                if (currentObj == null)
                {
                    return null;
                }
            }
            return childMember.GetValue(currentObj);
        }

        public override Object GetValue(Object obj, bool allowNullEquivalentValue)
        {
            Object currentObj = obj;
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                Member memberPathItem = memberPath[a];
                currentObj = memberPathItem.GetValue(currentObj, false);
                if (currentObj == null)
                {
                    return null;
                }
            }
            return childMember.GetValue(currentObj, allowNullEquivalentValue);
        }

        public override void SetValue(Object obj, Object value)
        {
            Object currentObj = obj;
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                Member memberPathItem = memberPath[a];
                Object childObj = memberPathItem.GetValue(currentObj, false);
                if (childObj == null)
                {
                    childObj = Activator.CreateInstance(memberPathItem.RealType);
                    memberPathItem.SetValue(currentObj, childObj);
                }
                currentObj = childObj;
            }
            childMember.SetValue(currentObj, value);
        }
    }
}