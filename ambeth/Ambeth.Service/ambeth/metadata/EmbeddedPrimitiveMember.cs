using System;
using System.Text;

namespace De.Osthus.Ambeth.Metadata
{
    public class EmbeddedPrimitiveMember : PrimitiveMember, IEmbeddedMember, IPrimitiveMemberWrite
    {
        private readonly PrimitiveMember childMember;

        private readonly Member[] memberPath;

        private readonly String name;

        public EmbeddedPrimitiveMember(String name, PrimitiveMember childMember, Member[] memberPath)
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

        public override bool TechnicalMember
        {
            get
            {
                return childMember.TechnicalMember;
            }
        }

        public void SetTechnicalMember(bool technicalMember)
        {
            ((IPrimitiveMemberWrite)childMember).SetTechnicalMember(technicalMember);
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

        public override Type EntityType
        {
            get
            {
                return memberPath[0].EntityType;
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

        public override bool IsToMany
        {
            get
            {
                return childMember.IsToMany;
            }
        }

        public override Attribute GetAnnotation(Type annotationType)
        {
            return childMember.GetAnnotation(annotationType);
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
                    throw new Exception("Should never be null at this point: " + ToString());
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
                    throw new Exception("Should never be null at this point: " + ToString());
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
                    throw new Exception("Should never be null at this point: " + ToString());
                }
                currentObj = childObj;
            }
            childMember.SetValue(currentObj, value);
        }
    }
}