using De.Osthus.Ambeth.Annotation;
using System;
using System.Text;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class EmbeddedRelationInfoItem : IEmbeddedTypeInfoItem, IRelationInfoItem
    {
        protected IRelationInfoItem childMember;

        protected ITypeInfoItem[] memberPath;

        protected String name;

        public EmbeddedRelationInfoItem(String name, IRelationInfoItem childMember, params ITypeInfoItem[] memberPath)
        {
            this.name = name;
            this.childMember = childMember;
            this.memberPath = memberPath;
        }

        public IRelationInfoItem ChildMember
        {
            get
            {
                return childMember;
            }
        }

        ITypeInfoItem IEmbeddedTypeInfoItem.ChildMember
        {
            get
            {
                return childMember;
            }
        }

        public ITypeInfoItem[] MemberPath
        {
            get
            {
                return memberPath;
            }
        }

        public String MemberPathString
        {
            get
            {
                StringBuilder sb = new StringBuilder();
                foreach (ITypeInfoItem member in MemberPath)
                {
                    if (sb.Length > 0)
                    {
                        sb.Append('.');
                    }
                    sb.Append(member.Name);
                }
                return sb.ToString();
            }
        }

        public String[] MemberPathToken
        {
            get
            {
                String[] token = new String[MemberPath.Length];
                for (int a = MemberPath.Length; a-- > 0; )
                {
                    ITypeInfoItem member = MemberPath[a];
                    token[a] = member.Name;
                }
                return token;
            }
        }

        public Type DeclaringType
        {
            get
            {
                return childMember.DeclaringType;
            }
        }

        public Object DefaultValue
        {
            get
            {
                return childMember.DefaultValue;
            }
            set
            {
                childMember.DefaultValue = value;
            }
        }

        public Object NullEquivalentValue
        {
            get
            {
                return childMember.NullEquivalentValue;
            }
            set
            {
                childMember.NullEquivalentValue = value;
            }
        }

        public Type RealType
        {
            get
            {
                return childMember.RealType;
            }
        }

        public Type ElementType
        {
            get
            {
                return childMember.ElementType;
            }
        }

        public bool TechnicalMember
        {
            get
            {
                return false;
            }
            set
            {
                throw new NotSupportedException("A relation can never be a technical member");
            }
        }

        public Object GetValue(Object obj)
        {
            return GetValue(obj, false);
        }

        public Object GetValue(Object obj, bool allowNullEquivalentValue)
        {
            Object currentObj = obj;
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                ITypeInfoItem memberPathItem = memberPath[a];
                currentObj = memberPathItem.GetValue(currentObj, allowNullEquivalentValue);
                if (currentObj == null)
                {
                    if (allowNullEquivalentValue)
                    {
                        return childMember.NullEquivalentValue;
                    }
                    return null;
                }
            }
            return childMember.GetValue(currentObj, allowNullEquivalentValue);
        }

        public void SetValue(Object obj, Object value)
        {
            Object currentObj = obj;
            for (int a = 0, size = memberPath.Length; a < size; a++)
            {
                ITypeInfoItem memberPathItem = memberPath[a];
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

        public V GetAnnotation<V>() where V : Attribute
        {
            return childMember.GetAnnotation<V>();
        }

        public bool CanRead
        {
            get
            {
                return childMember.CanRead;
            }
        }

        public bool CanWrite
        {
            get
            {
                return childMember.CanWrite;
            }
        }

        public String Name
        {
            get
            {
                return name;
            }
        }

        public String XMLName
        {
            get
            {
                return childMember.XMLName;
            }
        }

        public bool IsXMLIgnore
        {
            get
            {
                return childMember.IsXMLIgnore;
            }
        }

        public override String ToString()
        {
            return "Embedded: " + Name + "/" + XMLName + " " + childMember;
        }

        public CascadeLoadMode CascadeLoadMode
        {
            get
            {
                return childMember.CascadeLoadMode;
            }
        }

        public bool IsManyTo
        {
            get
            {
                return childMember.IsManyTo;
            }
        }

        public bool IsToMany
        {
            get
            {
                return childMember.IsToMany;
            }
        }
    }
}