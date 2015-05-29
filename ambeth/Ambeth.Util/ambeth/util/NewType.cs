using System;

namespace De.Osthus.Ambeth.Util
{
    public class NewType
    {
        public static readonly NewType VOID_TYPE = GetType(typeof(void));

        public static NewType GetType(Type type)
        {
            return new NewType(type, null);
        }

        public static NewType GetObjectType(String internalName)
        {
            return new NewType(null, internalName);
        }

        private readonly Type type;

        private readonly String internalName;

        private NewType(Type type, String internalName)
        {
            this.type = type;
            if (internalName == null)
            {
				if (type.FullName == null)
				{
					internalName = type.Name;
				}
				else
				{
					internalName = type.FullName.Replace('.', '/');
				}
            }
            this.internalName = internalName;
        }

        public Type Type
        {
            get
            {
                return type;
            }
        }

        public String ClassName
        {
            get
            {
                return internalName.Replace('/','.');
            }
        }

        public String InternalName
        {
            get
            {
                return internalName;
            }
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(obj, this))
            {
                return true;
            }
            if (!(obj is NewType))
            {
                return false;
            }
            NewType other = (NewType)obj;
            return internalName.Equals(other.internalName);
        }

        public override int GetHashCode()
        {
            return internalName.GetHashCode();
        }

        public override string ToString()
        {
            return InternalName;
        }
    }
}
