using System;
using System.Collections.Generic;
using System.Reflection;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#else
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Proxy
{
    public class InterfaceEntityInterceptor : IInitializingBean, IInterceptor
    {
	    public static readonly int ID_INDEX = 0;

	    private static readonly MethodInfo hashCodeMethod;

	    private static readonly MethodInfo equalsMethod;

	    static InterfaceEntityInterceptor()
	    {
			hashCodeMethod = typeof(Object).GetMethod("GetHashCode");
			equalsMethod = typeof(Object).GetMethod("Equals", new Type[]{ typeof(Object)});
	    }

        [LogInstance]
        public ILogger Log { private get; set; }

	    public Dictionary<MethodInfo, int?> MethodToIndex { protected get; set; }

	    protected Object[] propertyArray;

	    public void AfterPropertiesSet()
	    {
		    ParamChecker.AssertNotNull(MethodToIndex, "MethodToIndex");
		    propertyArray = new Object[MethodToIndex.Count / 2]; // 2 entries per property
	    }

        public void Intercept(IInvocation invocation)
        {
            MethodInfo method = invocation.Method;
		    int? indexHolder = DictionaryExtension.ValueOrDefault(MethodToIndex, method);
		    if (indexHolder == null)
		    {
			    if (method.Equals(hashCodeMethod))
			    {
				    Object id = propertyArray[ID_INDEX];
				    if (id != null)
				    {
					    invocation.ReturnValue = invocation.Proxy.GetType().GetHashCode() ^ id.GetHashCode();
                        return;
				    }
				    invocation.ReturnValue = GetHashCode(); // entity without id is not equal with anything beside itself
                    return;
			    }
			    else if (method.Equals(equalsMethod))
			    {
				    Object other = invocation.Arguments[0];
                    Object obj = invocation.Proxy;
				    if (other == obj)
				    {
                        invocation.ReturnValue = true;
					    return;
				    }
				    if (other == null)
				    {
                        invocation.ReturnValue = false;
                        return;
				    }
				    if (!obj.GetType().Equals(other.GetType()))
				    {
					    // Proxies must be of the same class
					    invocation.ReturnValue = false;
                        return;
				    }
				    Object id = propertyArray[ID_INDEX];
				    if (id == null)
				    {
                        invocation.ReturnValue = false;
					    return;
				    }
				    Object otherId = ((InterfaceEntityInterceptor) ((IProxyTargetAccessor) obj).GetInterceptors()[0]).propertyArray[ID_INDEX];
				    invocation.ReturnValue = id.Equals(otherId);
                    return;
			    }
			    throw new Exception("Method '" + method + "' is not supported by this interceptor");
		    }
		    int index = indexHolder.Value;
		    if (index < 0) // Negative values tell us that it is a setter call
		    {
			    propertyArray[-index - 1] = invocation.Arguments[0]; // Substract 1 to allow "-0" semantic
                invocation.ReturnValue = null;
		    }
		    else
		    {
			    invocation.ReturnValue = propertyArray[index];
		    }
	    }    
}
}
