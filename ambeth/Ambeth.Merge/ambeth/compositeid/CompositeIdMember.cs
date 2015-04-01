using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.CompositeId
{
    public class CompositeIdMember : PrimitiveMember, IPrimitiveMemberWrite
    {
        public static String FilterEmbeddedFieldName(String fieldName)
        {
            return fieldName.Replace(".", "__");
        }

        protected readonly Type declaringType;

        protected readonly Type realType;

        protected readonly PrimitiveMember[] members;

        protected readonly ConstructorInfo realTypeConstructorAccess;

        protected readonly Member[] fieldIndexOfMembers;

        protected readonly String name;

        protected bool technicalMember;

		protected bool isTransient;

        public CompositeIdMember(Type declaringType, Type realType, String name, PrimitiveMember[] members, IMemberTypeProvider memberTypeProvider)
        {
            this.declaringType = declaringType;
            this.realType = realType;
            this.name = name;
            this.members = members;
            fieldIndexOfMembers = new PrimitiveMember[members.Length];
            Type[] paramTypes = new Type[members.Length];
            for (int a = 0, size = members.Length; a < size; a++)
            {
                PrimitiveMember member = members[a];
                fieldIndexOfMembers[a] = memberTypeProvider.GetMember(realType, CompositeIdMember.FilterEmbeddedFieldName(member.Name));
                paramTypes[a] = member.RealType;
            }
            realTypeConstructorAccess = realType.GetConstructor(paramTypes);
        }
        
        public override Object NullEquivalentValue
        {
            get
            {
                return null;
            }
        }

        public virtual ConstructorInfo CreateInstanceOfCollection()
        {
            throw new NotSupportedException();
        }

        public override Type EntityType
        {
            get
            {
                return RealType;
            }
        }

        public override Type RealType
        {
            get
            {
                return realType;
            }
        }

        public override Type ElementType
        {
            get
            {
                return realType;
            }
        }

        public override Type DeclaringType
        {
            get
            {
                return declaringType;
            }
        }

        public override bool IsToMany
        {
            get
            {
                return false;
            }
        }

        public override bool CanRead
        {
            get
            {
                return true;
            }
        }

        public override bool CanWrite
        {
            get
            {
                return true;
            }
        }

        public override bool TechnicalMember
        {
            get
            {
                return technicalMember;
            }
        }

        public void SetTechnicalMember(bool technicalMember)
        {
            this.technicalMember = technicalMember;
        }

		public override bool Transient
		{
			get
			{
				return transient;
			}
		}

		public void SetTransient(bool isTransient)
		{
			this.isTransient = isTransient;
		}

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

        public PrimitiveMember[] Members
        {
            get
            {
                return members;
            }
        }

        public override Object GetValue(Object obj)
        {
            return GetValue(obj, true);
        }

        public override Object GetValue(Object obj, bool allowNullEquivalentValue)
        {
            PrimitiveMember[] members = this.members;
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

        public override void SetValue(Object obj, Object compositeId)
        {
            PrimitiveMember[] members = this.members;
            Member[] fieldIndexOfMembers = this.fieldIndexOfMembers;
            if (compositeId != null)
            {
                for (int a = members.Length; a-- > 0; )
                {
                    Member fieldIndexOfMember = fieldIndexOfMembers[a];
                    Object memberValue = fieldIndexOfMember.GetValue(compositeId);
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

        public override Attribute GetAnnotation(Type annotationType)
        {
            return null;
        }

        public override String Name
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