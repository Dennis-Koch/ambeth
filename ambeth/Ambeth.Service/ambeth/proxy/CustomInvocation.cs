using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public class CustomInvocation : IInvocation
    {
        public object[] Arguments { get; set; }

        public Type[] GenericArguments { get; set; }

        public object InvocationTarget { get; set; }

        public MethodInfo Method { get; set; }

        public MethodInfo MethodInvocationTarget { get; set; }

        public object Proxy { get; set; }

        public object ReturnValue { get; set; }

        public Type TargetType { get; set; }

        public object GetArgumentValue(int index)
        {
            return Arguments[index];
        }

        public MethodInfo GetConcreteMethod()
        {
            throw new NotSupportedException();
        }

        public MethodInfo GetConcreteMethodInvocationTarget()
        {
            throw new NotSupportedException();
        }

        public void Proceed()
        {
            throw new NotSupportedException();
        }

        public void SetArgumentValue(int index, object value)
        {
            Arguments[index] = value;
        }
    }
}
