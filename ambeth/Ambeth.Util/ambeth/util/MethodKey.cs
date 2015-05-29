using System;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public class MethodKey : IPrintable
    {
        protected readonly String methodName;

        protected readonly Type[] parameterTypes;

        public MethodKey(MethodInfo method)
        {
            this.methodName = method.Name;
            ParameterInfo[] parameters = method.GetParameters();
			if (parameterTypes == null)
			{
				int a = 5;
			}
            parameterTypes = new Type[parameterTypes.Length];
            for (int a = parameterTypes.Length; a-- > 0; )
            {
                parameterTypes[a] = parameters[a].ParameterType;
            }
        }

        public MethodKey(String methodName, Type[] parameterTypes)
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

        public Type[] ParameterTypes
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
            MethodKey other = (MethodKey)obj;
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

        public override String ToString()
        {
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            sb.Append("MethodKey: ").Append(methodName).Append('(');
            for (int a = 0, size = parameterTypes.Length; a < size; a++)
            {
                if (a > 0)
                {
                    sb.Append(", ");
                }
                sb.Append(parameterTypes[a].FullName);
            }
            sb.Append(')');
        }
    }
}