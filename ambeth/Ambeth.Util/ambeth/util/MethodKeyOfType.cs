using System;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public class MethodKeyOfType : IPrintable
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
                sb.Append(parameterTypes[a].ClassName);
		    }
            sb.Append(')');
	    }
    }
}