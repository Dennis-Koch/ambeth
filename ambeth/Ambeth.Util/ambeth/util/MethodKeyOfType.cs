using System;

namespace De.Osthus.Ambeth.Util
{
    public class MethodKeyOfType
    {
        protected readonly String methodName;

        protected readonly NewType[] parameterTypes;

        public MethodKeyOfType(String methodName, NewType[] parameterTypes)
        {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }

        public String MethodName
        {
            get
            {
                return methodName;
            }
        }

        public NewType[] ParameterTypes
        {
            get
            {
                return parameterTypes;
            }
        }

        public override int GetHashCode()
        {
            return methodName.GetHashCode() ^ parameterTypes.Length;
        }

        public override bool Equals(Object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (GetType() != obj.GetType())
            {
                return false;
            }
            MethodKeyOfType other = (MethodKeyOfType)obj;
            if (!Object.Equals(methodName, other.methodName))
            {
                return false;
            }
            if (!Arrays.Equals(parameterTypes, other.parameterTypes))
            {
                return false;
            }
            return true;
        }
    }
}