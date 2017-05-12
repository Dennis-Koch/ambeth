using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Security.Transfer;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Service
{
    public sealed class SyncToAsyncUtil
    {
        private static readonly IDictionary<MethodInfo, MethodInfo[]> syncToAsyncDict = new Dictionary<MethodInfo, MethodInfo[]>();

        private static readonly IDictionary<MethodInfo, MethodInfo> asyncToSyncDict = new Dictionary<MethodInfo, MethodInfo>();

        private static readonly Lock readLock, writeLock;

        private static readonly HashSet<Type> lowLevelSerializationTypes = new HashSet<Type>();

        static SyncToAsyncUtil()
        {
            lowLevelSerializationTypes.Add(typeof(Type));
            lowLevelSerializationTypes.Add(typeof(MethodDescription));
            lowLevelSerializationTypes.Add(typeof(ServiceDescription));
            lowLevelSerializationTypes.Add(typeof(SecurityScope));
            lowLevelSerializationTypes.Add(typeof(List<Type>));
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

        public static bool IsLowLevelSerializationType(Type type)
        {
            if (typeof(Type).IsAssignableFrom(type) || typeof(IEnumerable).IsAssignableFrom(type) || type.IsArray || type.IsValueType || type.IsEnum || type.IsPrimitive || ImmutableTypeSet.IsImmutableType(type))
            {
                return true;
            }
            return lowLevelSerializationTypes.Contains(type);
        }

        public static MethodInfo GetSyncMethod(MethodInfo asyncBeginMethod, Type syncInterface)
        {
            readLock.Lock();
            try
            {
                MethodInfo syncMethod = DictionaryExtension.ValueOrDefault(asyncToSyncDict, asyncBeginMethod);
                if (syncMethod != null)
                {
                    return syncMethod;
                }
            }
            finally
            {
                readLock.Unlock();
            }
            ParameterInfo[] parameters = asyncBeginMethod.GetParameters();
            String asyncMethodName = asyncBeginMethod.Name;
            String syncMethodName;
            Type[] paramTypes;
            if (asyncMethodName.StartsWith("Begin"))
            {
                syncMethodName = asyncMethodName.Substring(5);
                paramTypes = new Type[parameters.Length - 2]; // Trim last 2 arguments "IAsyncCallback" abd "Objectstate"
                for (int a = parameters.Length - 2; a-- > 0; )
                {
                    ParameterInfo parameter = parameters[a];
                    paramTypes[a] = parameter.ParameterType;
                }
            }
            else
            {
                throw new ArgumentException("AsyncMethod '" + asyncBeginMethod + "' does not seem to be a Begin-method");
            }
            MethodInfo result = syncInterface.GetMethod(syncMethodName, paramTypes);
            if (result == null)
            {
                throw new ArgumentException("No method with name '" + syncMethodName + "'" + paramTypes + " found");
            }
            writeLock.Lock();
            try
            {
                if (!asyncToSyncDict.ContainsKey(asyncBeginMethod)) // Some other thread might be faster here so we have to check this
                {
                    asyncToSyncDict.Add(asyncBeginMethod, result);
                }
            }
            finally
            {
                writeLock.Unlock();
            }
            return result;
        }

        public static MethodInfo[] GetAsyncMethods(MethodInfo syncMethod, Type asyncInterface)
        {
            readLock.Lock();
            try
            {
                MethodInfo[] asyncMethods = DictionaryExtension.ValueOrDefault(syncToAsyncDict, syncMethod);
                if (asyncMethods != null)
                {
                    return asyncMethods;
                }
            }
            finally
            {
                readLock.Unlock();
            }
            ParameterInfo[] parameters = syncMethod.GetParameters();

            Type[] beginTypes = new Type[parameters.Length + 2];
            for (int a = parameters.Length; a-- > 0; )
            {
                beginTypes[a] = parameters[a].ParameterType;
            }
            beginTypes[beginTypes.Length - 2] = typeof(AsyncCallback);
            beginTypes[beginTypes.Length - 1] = typeof(Object);

            String methodName = syncMethod.Name;
            String beginName = "Begin" + methodName;
            String endName = "End" + methodName;
            MethodInfo beginMethod = asyncInterface.GetMethod(beginName, beginTypes);

            if (beginMethod == null)
            {
                throw new ArgumentException("No method with name '" + beginName + "'" + parameters + " found");
            }
            MethodInfo endMethod = asyncInterface.GetMethod(endName, new Type[] { typeof(IAsyncResult) });
            if (endMethod == null)
            {
                throw new ArgumentException("No method with name '" + endName + "'" + typeof(IAsyncResult) + " found");
            }
            MethodInfo[] result = new MethodInfo[] { beginMethod, endMethod };
            writeLock.Lock();
            try
            {
                if (!syncToAsyncDict.ContainsKey(syncMethod)) // Some other thread might be faster here so we have to check this
                {
                    syncToAsyncDict.Add(syncMethod, result);
                }
                if (!asyncToSyncDict.ContainsKey(beginMethod))
                {
                    asyncToSyncDict.Add(beginMethod, syncMethod);
                }
            }
            finally
            {
                writeLock.Unlock();
            }
            return result;
        }

        public static Object[] BuildSyncArguments(Object[] asyncArguments, out AsyncCallback asyncCallback)
        {
            asyncCallback = (AsyncCallback)asyncArguments[asyncArguments.Length - 2];
            Object[] syncArguments = new Object[asyncArguments.Length - 2];
            Array.Copy(asyncArguments, syncArguments, syncArguments.Length);
            return syncArguments;
        }

        public static ServiceDescription CreateServiceDescription(String serviceName, MethodInfo syncMethod, Object[] syncArguments, params ISecurityScope[] securityScopes)
        {
            Type[] types = GetMethodParamTypes(syncMethod);
            ServiceDescription serviceDescription = new ServiceDescription();
            serviceDescription.ServiceName = serviceName;
            serviceDescription.MethodName = syncMethod.Name;
            serviceDescription.ParamTypes = types;
            serviceDescription.Arguments = syncArguments;
            ISecurityScope[] securityScopesWeb = new ISecurityScope[securityScopes.Length];
            Array.Copy(securityScopes, 0, securityScopesWeb, 0, securityScopes.Length);
            serviceDescription.SecurityScopes = securityScopesWeb;
            return serviceDescription;
        }

        public static MethodDescription CreateMethodDescription(MethodInfo syncMethod)
        {
            Type[] types = GetMethodParamTypes(syncMethod);
            MethodDescription methodDescription = new MethodDescription();
            methodDescription.MethodName = syncMethod.Name;
            methodDescription.ParamTypes = types;
            methodDescription.ServiceType = syncMethod.DeclaringType;
            return methodDescription;
        }

        private static Type[] GetMethodParamTypes(MethodInfo method)
        {
            ParameterInfo[] parameters = method.GetParameters();
            Type[] types = new Type[parameters.Length];
            for (int a = parameters.Length; a-- > 0; )
            {
                types[a] = parameters[a].ParameterType;
            }
            return types;
        }

        private SyncToAsyncUtil()
        {
            // intended blank
        }
    }
}