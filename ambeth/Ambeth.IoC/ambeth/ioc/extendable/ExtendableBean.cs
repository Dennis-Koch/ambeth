using System;
using System.Collections.Generic;
using System.Reflection;
using Castle.DynamicProxy;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Config;
#if SILVERLIGHT
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public class ExtendableBean : IFactoryBean, IInitializingBean, IInterceptor
    {
        public static readonly String P_PROVIDER_TYPE = "ProviderType";

        public static readonly String P_EXTENDABLE_TYPE = "ExtendableType";

        public static readonly String P_DEFAULT_BEAN = "DefaultBean";

        protected static readonly Object[] oneArgs = new Object[] { typeof(Object) };

        protected static readonly Type[] classObjectArgs = new Type[] { typeof(Object) };

        protected static readonly Type[] stringArgs = new Type[] { typeof(String) };

        public static IBeanConfiguration RegisterExtendableBean(IBeanContextFactory beanContextFactory, Type providerType, Type extendableType)
        {
            return RegisterExtendableBean(beanContextFactory, null, providerType, extendableType);
        }

        public static IBeanConfiguration RegisterExtendableBean(IBeanContextFactory beanContextFactory, String beanName, Type providerType,
                Type extendableType)
        {
            if (beanName != null)
            {
                return beanContextFactory.RegisterBean(beanName, typeof(ExtendableBean)).PropertyValue(ExtendableBean.P_PROVIDER_TYPE, providerType)
                        .PropertyValue(ExtendableBean.P_EXTENDABLE_TYPE, extendableType).Autowireable(providerType, extendableType);
            }
            return beanContextFactory.RegisterAnonymousBean(typeof(ExtendableBean)).PropertyValue(ExtendableBean.P_PROVIDER_TYPE, providerType)
                    .PropertyValue(ExtendableBean.P_EXTENDABLE_TYPE, extendableType).Autowireable(providerType, extendableType);
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        public IExtendableRegistry ExtendableRegistry { protected get; set; }

        public IProxyFactory ProxyFactory { protected get; set; }

        public Type ProviderType { protected get; set; }

        public Type ExtendableType { protected get; set; }

        public Object DefaultBean { protected get; set; }

        public Type[] ArgumentTypes { protected get; set; }

        protected Object extendableContainer;

        protected MethodInfo providerTypeGetOne = null;

        protected readonly IDictionary<MethodInfo, MethodInfo> methodMap = new Dictionary<MethodInfo, MethodInfo>();

        protected Object proxy;

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ExtendableRegistry, "ExtendableRegistry");
            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
            ParamChecker.AssertNotNull(ProviderType, "ProviderType");
            ParamChecker.AssertNotNull(ExtendableType, "ExtendableType");

            MethodInfo[] addRemoveMethods;
            if (ArgumentTypes != null)
            {
                addRemoveMethods = ExtendableRegistry.GetAddRemoveMethods(ExtendableType, ArgumentTypes);
            }
            else
            {
                addRemoveMethods = ExtendableRegistry.GetAddRemoveMethods(ExtendableType);
            }
            MethodInfo addMethod = addRemoveMethods[0];
            MethodInfo removeMethod = addRemoveMethods[1];

            ParameterInfo[] parameterTypes = addMethod.GetParameters();
            Type extensionType = parameterTypes[0].ParameterType;

            if (parameterTypes.Length == 1)
            {
                extendableContainer = Activator.CreateInstance(typeof(DefaultExtendableContainer<>).MakeGenericType(extensionType), "message");

                Type[] extTypeArray = new Type[] { extensionType };
                Type fastClass = extendableContainer.GetType();
                MethodInfo registerMethod = fastClass.GetMethod("Register", extTypeArray);
                MethodInfo unregisterMethod = fastClass.GetMethod("Unregister", extTypeArray);
                MethodInfo getAllMethod = fastClass.GetMethod("GetExtensions", EmptyList.EmptyArray<Type>());
                MethodInfo[] methodsOfProviderType = ProviderType.GetMethods();

                methodMap.Add(addMethod, registerMethod);
                methodMap.Add(removeMethod, unregisterMethod);

                for (int a = methodsOfProviderType.Length; a-- > 0; )
                {
                    MethodInfo methodOfProviderType = methodsOfProviderType[a];
                    if (methodOfProviderType.GetParameters().Length == 0)
                    {
                        methodMap.Add(methodOfProviderType, getAllMethod);
                    }
                }
            }
            else if (parameterTypes.Length == 2)
            {
                Type keyType = parameterTypes[1].ParameterType;
                if (typeof(Type).Equals(keyType))
                {
                    extendableContainer = Activator.CreateInstance(typeof(ClassExtendableContainer<>).MakeGenericType(extensionType), "message", "keyMessage");
                }
                else
                {
                    extendableContainer = Activator.CreateInstance(typeof(MapExtendableContainer<,>).MakeGenericType(keyType, extensionType), "message", "keyMessage");
                }
                Type[] extKeyTypeArray = new Type[] { extensionType, keyType };
                Type fastClass = extendableContainer.GetType();
                MethodInfo registerMethod = fastClass.GetMethod("Register", extKeyTypeArray);
                MethodInfo unregisterMethod = fastClass.GetMethod("Unregister", extKeyTypeArray);
                MethodInfo getOneMethod = fastClass.GetMethod("GetExtension", new Type[] { keyType });
                MethodInfo getAllMethod = fastClass.GetMethod("GetExtensions", EmptyList.EmptyArray<Type>());
                MethodInfo[] methodsOfProviderType = ProviderType.GetMethods();

                methodMap.Add(addMethod, registerMethod);
                methodMap.Add(removeMethod, unregisterMethod);

                for (int a = methodsOfProviderType.Length; a-- > 0; )
                {
                    MethodInfo methodOfProviderType = methodsOfProviderType[a];
                    if (methodOfProviderType.GetParameters().Length == 1)
                    {
                        methodMap.Add(methodOfProviderType, getOneMethod);
                        providerTypeGetOne = methodOfProviderType;
                    }
                    else if (methodOfProviderType.GetParameters().Length == 0)
                    {
                        methodMap.Add(methodOfProviderType, getAllMethod);
                    }
                }

            }
            else
            {
                throw new ExtendableException("ExtendableType '" + ExtendableType.FullName
                        + "' not supported: It must contain exactly 2 methods with each either 1 or 2 arguments");
            }
        }

        public Object GetObject()
        {
            if (proxy == null)
            {
                proxy = ProxyFactory.CreateProxy(new Type[] { ProviderType, ExtendableType }, this);
            }
            return proxy;
        }

        public void Intercept(IInvocation invocation)
        {
            MethodInfo mappedMethod = DictionaryExtension.ValueOrDefault(methodMap, invocation.Method);
            if (mappedMethod == null)
            {
                invocation.ReturnValue = invocation.Method.Invoke(extendableContainer, invocation.Arguments);
                return;
            }
            Object value = mappedMethod.Invoke(extendableContainer, invocation.Arguments);
            if (value == null && invocation.Method.Equals(providerTypeGetOne))
            {
                value = DefaultBean;
            }
            invocation.ReturnValue = value;
        }
    }
}