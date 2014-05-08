using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
using Castle.DynamicProxy;
#endif
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Service.Interceptor;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security.Interceptor
{
    public class SecurityScopeInterceptor : AbstractInterceptor<ISecurityService, ISecurityClient>
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public static SecurityScopeInterceptor Create(
            ISecurityService securityService,
            ISecurityClient securityClient,
            ISecurityScope[] securityScopes,
            String serviceName)
        {
            SecurityScopeInterceptor interceptor = new SecurityScopeInterceptor();
            interceptor.Service = securityService;
            interceptor.Client = securityClient;
            interceptor.SecurityScopes = securityScopes;
            interceptor.ServiceName = serviceName;
            return interceptor;
        }

        public ISecurityScope[] SecurityScopes { get; set; }

        public String ServiceName { get; set; }

        protected override Object InterceptApplication(IInvocation invocation,
            Boolean? isAsyncBegin)
        {
            Object[] arguments = invocation.Arguments;
            MethodInfo method = invocation.Method;
            if (isAsyncBegin.HasValue && !isAsyncBegin.Value)
            {
                return ((IAsyncResult)arguments[0]).AsyncState;
            }
            if (isAsyncBegin.HasValue)
            {
                throw new NotSupportedException("Not supported any more");
                //AsyncCallback asyncCallback;
                //Object[] syncArguments = SyncToAsyncUtil.BuildSyncArguments(arguments, out asyncCallback);

                //MethodInfo syncMethod = SyncToAsyncUtil.GetSyncMethod(method, GetSyncServiceType());

                //ServiceDescription serviceDescription = SyncToAsyncUtil.CreateServiceDescription(ServiceName, syncMethod, syncArguments);

                //IAsyncResult asyncResult = Client.BeginCallServiceInSecurityScope(SecurityScopes, serviceDescription, delegate(IAsyncResult ar)
                //{
                //    try
                //    {
                //        Object scopedResult = Client.EndCallServiceInSecurityScope(ar);
                //        if (asyncCallback != null)
                //        {
                //            asyncCallback.Invoke(new AsyncResult(scopedResult));
                //        }
                //    }
                //    catch (Exception e)
                //    {
                //        if (Log.ErrorEnabled)
                //        {
                //            Log.Error(e);
                //        }
                //        throw;
                //    }
                //}, null);
                //return asyncResult;
            }
            return Intercept(ServiceName, method, arguments);
        }
        
        public virtual Object Intercept(String customServiceName, MethodInfo method, params Object[] arguments)
        {
            ServiceDescription serviceDescription = SyncToAsyncUtil.CreateServiceDescription(customServiceName, method, arguments);
            return Service.CallServiceInSecurityScope(SecurityScopes, serviceDescription);
        }

        protected override Object InterceptLoad(IInvocation invocation,
            Boolean? isAsyncBegin)
        {
            return InterceptApplication(invocation, isAsyncBegin);
        }

        protected override Object InterceptMerge(IInvocation invocation,
            Boolean? isAsyncBegin)
        {
            return InterceptApplication(invocation, isAsyncBegin);
        }

        protected override Object InterceptDelete(IInvocation invocation,
            Boolean? isAsyncBegin)
        {
            return InterceptApplication(invocation, isAsyncBegin);
        }

        protected override Object InterceptLoadIntern(MethodInfo method, Object[] arguments,
            Boolean? isAsyncBegin, Object result)
        {
            throw new NotSupportedException();
        }

        protected override Object InterceptMergeIntern(MethodInfo method, Object[] arguments,
            Boolean? isAsyncBegin)
        {
            throw new NotSupportedException();
        }

        protected override Object InterceptDeleteIntern(MethodInfo method, Object[] arguments,
            Boolean? isAsyncBegin)
        {
            throw new NotSupportedException();
        }
    }
}