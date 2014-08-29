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
    public abstract class CascadedInterceptor : AbstractSimpleInterceptor, ICascadedInterceptor
    {
        public Object Target { get; set; }
        
        protected virtual void InvokeTarget(IInvocation invocation)
        {
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