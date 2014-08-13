using System;
using System.Reflection;
using System.Text;
#if !SILVERLIGHT
using Castle.DynamicProxy;
using System.Threading;
#else
using Castle.Core.Interceptor;
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Proxy
{
    public abstract class CascadedInterceptor : ICascadedInterceptor
    {
        public static readonly MethodInfo equalsMethod = ReflectUtil.GetDeclaredMethod(false, typeof(Object), typeof(bool), "Equals", typeof(Object));

        public Object Target { get; set; }
        
        abstract public void Intercept(IInvocation invocation);

        protected virtual void InvokeTarget(IInvocation invocation)
        {
            if (equalsMethod.Equals(invocation.Method))
            {
                if (Object.ReferenceEquals(invocation.Proxy, invocation.Arguments[0]))
                {
                    invocation.ReturnValue = true;
                    return;
                }
            }
            if (Target == null)
            {
                throw new NullReferenceException("Target must be valid");
            }
            if (Target is IInterceptor)
            {
                ((IInterceptor)Target).Intercept(invocation);
            }
            else
            {
                try
                {
                    invocation.ReturnValue = invocation.Method.Invoke(Target, invocation.Arguments);
                }
                catch (Exception e)
                {
                    Type[] allInterfaces = Target.GetType().GetInterfaces();
                    StringBuilder sb = new StringBuilder();
                    bool first = true;
                    for (int a = allInterfaces.Length; a-- > 0; )
                    {
                        if (first)
                        {
                            first = false;
                        }
                        else
                        {
                            sb.Append(',');
                        }
                        sb.Append(allInterfaces[a].FullName);
                    }
                    throw new Exception(sb.ToString(), e);
                }
            }
        }
    }
}