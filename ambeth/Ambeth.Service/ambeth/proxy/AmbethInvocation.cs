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
    public class AmbethInvocation : IInvocation
    {
        public IInvocation WrappedInvocation { get; set; }
        
        public object[] Arguments
        {
            get
            {
                return WrappedInvocation.Arguments;
            }
        }

        public Type[] GenericArguments
        {
            get
            {
                return WrappedInvocation.GenericArguments;
            }
        }

        public object InvocationTarget
        {
            get
            {
                return WrappedInvocation.InvocationTarget;
            }
        }

        public MethodInfo Method { get; set; }

        public MethodInfo MethodInvocationTarget
        {
            get
            {
                return WrappedInvocation.MethodInvocationTarget;
            }
        }

        public object Proxy
        {
            get
            {
                return WrappedInvocation.Proxy;
            }
        }

        public object ReturnValue
        {
            get
            {
                return WrappedInvocation.ReturnValue;
            }
            set
            {
                WrappedInvocation.ReturnValue = value;
            }
        }

        public Type TargetType
        {
            get
            {
                return WrappedInvocation.TargetType;
            }
        }

        public object GetArgumentValue(int index)
        {
            return WrappedInvocation.GetArgumentValue(index);
        }

        public MethodInfo GetConcreteMethod()
        {
            return WrappedInvocation.GetConcreteMethod();
        }

        public MethodInfo GetConcreteMethodInvocationTarget()
        {
            return WrappedInvocation.GetConcreteMethodInvocationTarget();
        }

        public void Proceed()
        {
            WrappedInvocation.Proceed();
        }

        public void SetArgumentValue(int index, object value)
        {
            WrappedInvocation.SetArgumentValue(index, value);
        }
    }
}
