using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Security;
using System.Collections;
using System.Reflection;
using System.Security.Principal;
using System.ServiceModel;
using De.Osthus.Ambeth.Proxy;using System.Threading;
using De.Osthus.Ambeth.Util;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public class WCFUnwrapInterceptor : CascadedInterceptor
    {
        public static WCFUnwrapInterceptor Create(Type wcfInterfaceType)
        {
            WCFUnwrapInterceptor interceptor = new WCFUnwrapInterceptor();
            interceptor.InterfaceType = wcfInterfaceType;
            return interceptor;
        }

        protected ThreadLocal<IDictionary<MethodInfo, MethodInfo>> externToInternMethodDictTL = new ThreadLocal<IDictionary<MethodInfo, MethodInfo>>(
            delegate()
            {
                return new Dictionary<MethodInfo, MethodInfo>();
            }
        );

        public Type InterfaceType { get; set; }

        public override void Intercept(IInvocation invocation)
        {
            MethodInfo originalMethod = invocation.Method;
            MethodInfo method = FindMethodOnInterface(originalMethod);

            AmbethInvocation ctiInvocation = new AmbethInvocation();
            ctiInvocation.Method = method;
            ctiInvocation.WrappedInvocation = invocation;

            InvokeTarget(ctiInvocation);

            invocation.ReturnValue = PostProcessResult(ctiInvocation.ReturnValue, originalMethod);
        }

        protected Object PostProcessResult(Object returnValue, MethodInfo originalMethod)
        {
            if (returnValue == null)
            {
                return returnValue;
            }
            Type returnType = returnValue.GetType();
            Type expectedReturnType = originalMethod.ReturnType;
            if (expectedReturnType.IsAssignableFrom(returnType))
            {
                return returnValue;
            }
            if (expectedReturnType.IsClass && typeof(IList<>).IsAssignableFrom(expectedReturnType))
            {
                return Activator.CreateInstance(expectedReturnType, returnValue);
            }
            throw new ArgumentException("Argument of '" + returnValue + "' can not be converted to expected type '" + expectedReturnType + "'");
        }

        protected MethodInfo FindMethodOnInterface(MethodInfo classMethod)
        {
            IDictionary<MethodInfo, MethodInfo> externToInternMethodDict = externToInternMethodDictTL.Value;

            MethodInfo interfaceMethod = DictionaryExtension.ValueOrDefault(externToInternMethodDict, classMethod);
            if (interfaceMethod != null)
            {
                return interfaceMethod;
            }
            Type[] types = GetTypes(classMethod.GetParameters());
            MethodInfo[] methods = InterfaceType.GetMethods();
            foreach (MethodInfo method in methods)
            {
                if (!classMethod.Name.StartsWith(method.Name))
                {
                    continue;
                }
                // This is a method candidate tested by its name. The question is now how the arguments are typed
                bool paramFails = false;
                ParameterInfo[] paramInfos = method.GetParameters();
                if (paramInfos.Length != types.Length)
                {
                    continue;
                }
                for (int a = paramInfos.Length; a-- > 0; )
                {
                    Type parameterType = paramInfos[a].ParameterType;
                    if (!types[a].Equals(parameterType))
                    {
                        paramFails = true;
                        break;
                    }
                }
                if (paramFails)
                {
                    continue;
                }
                interfaceMethod = method;
                break;
            }
            if (interfaceMethod == null)
            {
                throw new NotSupportedException("Class method: '" + classMethod + "' can not be mapped to a method of interface '" + InterfaceType + "'");
            }
            externToInternMethodDict.Add(classMethod, interfaceMethod);
            return interfaceMethod;
        }

        protected Type[] GetTypes(ParameterInfo[] parameters)
        {
            Type[] types = new Type[parameters.Length];
            for (int a = parameters.Length; a-- > 0; )
            {
                Type parameterType = parameters[a].ParameterType;
                if (parameterType.IsClass && typeof(IList<>).IsAssignableFrom(parameterType))
                {
                    Type[] genericArguments = parameterType.GetGenericArguments();
                    parameterType = typeof(IList<Object>).MakeGenericType(genericArguments[0]);
                }
                types[a] = parameterType;
            }
            return types;
        }
    }
}
