using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.CompositeId
{
    public class CompositeIdTypeInfoItem : ITypeInfoItem
    {
        public static String FilterEmbeddedFieldName(String fieldName)
        {
            return fieldName.Replace(".", "__");
        }

        protected readonly Type declaringType;

        protected readonly Type realType;

        protected readonly ITypeInfoItem[] members;

        protected readonly ConstructorInfo realTypeConstructorAccess;

        protected readonly ITypeInfoItem[] fieldIndexOfMembers;

        protected readonly String name;

        public CompositeIdTypeInfoItem(Type declaringType, Type realType, String name, ITypeInfoItem[] members)
        {
            this.declaringType = declaringType;
            this.realType = realType;
            this.name = name;
            this.members = members;
            fieldIndexOfMembers = new ITypeInfoItem[members.Length];
            Type[] paramTypes = new Type[members.Length];
            for (int a = 0, size = members.Length; a < size; a++)
            {
                ITypeInfoItem member = members[a];
                fieldIndexOfMembers[a] = new FieldInfoItemASM(realType.GetField(CompositeIdTypeInfoItem.FilterEmbeddedFieldName(member.Name), BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance));
                paramTypes[a] = member.RealType;
            }
            realTypeConstructorAccess = realType.GetConstructor(paramTypes);
        }

        public Object DefaultValue
        {
            get
            {
                return null;
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        public Object NullEquivalentValue
        {
            get
            {
                return null;
            }
            set
            {
                throw new NotSupportedException();
            }
        }

        public virtual ConstructorInfo CreateInstanceOfCollection()
        {
            throw new NotSupportedException();
        }

        public Type RealType
        {
            get
            {
                return realType;
            }
        }

        public Type ElementType
        {
            get
            {
                return realType;
            }
        }

        public Type DeclaringType
        {
            get
            {
                return declaringType;
            }
        }

        public bool CanRead
        {
            get
            {
                return true;
            }
        }

        public bool CanWrite
        {
            get
            {
                return true;
            }
        }

        public bool TechnicalMember { get; set; }

        public Object GetDecompositedValue(Object compositeId, int compositeMemberIndex)
        {
            return members[compositeMemberIndex].GetValue(compositeId, false);
        }

        public Object GetDecompositedValueOfObject(Object obj, int compositeMemberIndex)
        {
            return members[compositeMemberIndex].GetValue(obj, false);
        }

        public ConstructorInfo GetRealTypeConstructorAccess()
        {
            return realTypeConstructorAccess;
        }

        public ITypeInfoItem[] FieldIndexOfMembers
        {
            get
            {
                return fieldIndexOfMembers;
            }
        }

        public ITypeInfoItem[] Members
        {
            get
            {
                return members;
            }
        }

        public Object GetValue(Object obj)
        {
            return GetValue(obj, true);
        }

        public Object GetValue(Object obj, bool allowNullEquivalentValue)
        {
            ITypeInfoItem[] members = this.members;
            Object[] args = new Object[members.Length];
            for (int a = members.Length; a-- > 0; )
            {
                Object memberValue = members[a].GetValue(obj, allowNullEquivalentValue);
                if (memberValue == null)
                {
                    return null;
                }
                args[a] = memberValue;
            }
            return realTypeConstructorAccess.Invoke(args);
        }

        public void SetValue(Object obj, Object compositeId)
        {
            ITypeInfoItem[] members = this.members;
            ITypeInfoItem[] fieldIndexOfMembers = this.fieldIndexOfMembers;
            if (compositeId != null)
            {
                for (int a = members.Length; a-- > 0; )
                {
                    ITypeInfoItem fieldIndexOfMember = fieldIndexOfMembers[a];
                    Object memberValue = fieldIndexOfMember.GetValue(compositeId, false);
                    members[a].SetValue(obj, memberValue);
                }
            }
            else
            {
                for (int a = members.Length; a-- > 0; )
                {
                    members[a].SetValue(obj, null);
                }
            }
        }

        public V GetAnnotation<V>() where V : Attribute
        {
            throw new NotSupportedException();
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
                throw new NotSupportedException();
            }
        }

        public bool IsXMLIgnore
        {
            get
            {
                return true;
            }
        }

        public override String ToString()
        {
            return Name;
        }
    }
}