using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Security;
using System.Collections;
using System.Reflection;
using System.Security.Principal;
using System.ServiceModel;
using System.Threading;
using De.Osthus.Ambeth.Util;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    public class WCFWrapInterceptor : CascadedInterceptor
    {
        public static WCFWrapInterceptor Create(Type wcfInterfaceType)
        {
            WCFWrapInterceptor interceptor = new WCFWrapInterceptor();
            interceptor.WCFInterfaceType = wcfInterfaceType;
            return interceptor;
        }

        protected ThreadLocal<IDictionary<MethodInfo, MethodInfo>> externToInternMethodDictTL = new ThreadLocal<IDictionary<MethodInfo, MethodInfo>>(
            delegate()
            {
                return new Dictionary<MethodInfo, MethodInfo>();
            }
        );

        public Type WCFInterfaceType { get; set; }

        public override void Intercept(IInvocation invocation)
        {
            MethodInfo method = FindMethodOnInterface(invocation.Method);

            AmbethInvocation ctiInvocation = new AmbethInvocation();
            ctiInvocation.Method = method;
            ctiInvocation.WrappedInvocation = invocation;

            InvokeTarget(ctiInvocation);

            invocation.ReturnValue = PostProcessResult(ctiInvocation.ReturnValue);
        }

        protected Object PostProcessResult(Object resultValue)
        {
            return resultValue;
        }

        protected MethodInfo FindMethodOnInterface(MethodInfo classMethod)
        {
            IDictionary<MethodInfo, MethodInfo> externToInternMethodDict = externToInternMethodDictTL.Value;

            MethodInfo interfaceMethod = DictionaryExtension.ValueOrDefault(externToInternMethodDict, classMethod);
            if (interfaceMethod != null)
            {
                return interfaceMethod;
            }
            ParameterInfo[] classMethodParams = classMethod.GetParameters();
            Type[] classMethodTypes = GetTypes(classMethodParams);

            List<Type> remainingInterfaces = new List<Type>();
            remainingInterfaces.Add(WCFInterfaceType);
            while (remainingInterfaces.Count > 0 && interfaceMethod == null)
            {
                Type currInterface = remainingInterfaces[0];
                remainingInterfaces.RemoveAt(0);
                MethodInfo[] methods = currInterface.GetMethods();
                foreach (MethodInfo method in methods)
                {
                    if (!method.Name.StartsWith(classMethod.Name))
                    {
                        continue;
                    }
                    String nameSuffix = method.Name.Substring(classMethod.Name.Length);
                    if (!String.IsNullOrEmpty(nameSuffix))
                    {
                        try
                        {
                            int suffixId = Int32.Parse(nameSuffix);
                        }
                        catch (Exception)
                        {
                            // Intended blank
                            continue;
                        }
                    }
                    // This is a method candidate tested by its name. The question is now how the arguments are typed
                    bool paramFails = false;
                    ParameterInfo[] paramInfos = method.GetParameters();
                    if (paramInfos.Length != classMethodTypes.Length)
                    {
                        continue;
                    }
                    for (int a = paramInfos.Length; a-- > 0; )
                    {
                        Type parameterType = paramInfos[a].ParameterType;
                        Type classMethodType = classMethodTypes[a];

                        if (!classMethodType.Equals(parameterType) && !classMethodParams[a].ParameterType.Equals(parameterType))
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
                    remainingInterfaces.AddRange(currInterface.GetInterfaces());
                }
            }
            //interfaceMethod = WCFInterfaceType.GetMethod(classMethod.Name, types);
            if (interfaceMethod == null)
            {
                throw new NotSupportedException("Class method: '" + classMethod + "' can not be mapped to a method of interface '" + WCFInterfaceType + "'");
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
