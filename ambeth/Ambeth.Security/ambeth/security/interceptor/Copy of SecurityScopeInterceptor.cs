//using System;
//using System.Collections;
//using System.Collections.Generic;
//using System.Reflection;
//#if !SILVERLIGHT
//using Castle.DynamicProxy;
//#else
//using Castle.Core.Interceptor;
//using Castle.DynamicProxy;
//#endif
//using De.Osthus.Ambeth.Security;
//using De.Osthus.Ambeth.Service;
//using De.Osthus.Ambeth.Util;
//using De.Osthus.Ambeth.Proxy;
//using De.Osthus.Ambeth.Service.Interceptor;
//using De.Osthus.Ambeth.Transfer;
//using De.Osthus.Ambeth.Log;
//using De.Osthus.Ambeth.Ioc;

//namespace De.Osthus.Ambeth.Security.Interceptor
//{
//    public class SecurityScopeInterceptor : IInterceptor, IInitializingBean
//    {
//        [LogInstance]
//      	public ILogger Log { private get; set; }

//        public ISecurityScope[] SecurityScopes { get; set; }

//        public ISecurityService SecurityService { get; set; }

//        public String ServiceName { get; set; }

//        public virtual void AfterPropertiesSet()
//        {
//            ParamChecker.AssertNotNull(SecurityService, "SecurityService");
//        }

//        public virtual void Intercept(IInvocation invocation)
//        {
//            Object[] arguments = invocation.Arguments;
//            MethodInfo method = invocation.Method;

//            invocation.ReturnValue = Intercept(ServiceName, invocation.Method, invocation.Arguments);
//        }

//        public virtual Object Intercept(String customServiceName, MethodInfo method, params Object[] arguments)
//        {
//            ServiceDescription serviceDescription = SyncToAsyncUtil.CreateServiceDescription(customServiceName, method, arguments);
//            return SecurityService.CallServiceInSecurityScope(SecurityScopes, serviceDescription);
//        }
//    }
//}