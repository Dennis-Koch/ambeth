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
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Security.Interceptor
{
    public class ProgressInterceptor : AbstractInterceptor<IProgressService, IProgressClient>, IInitializingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public String ServiceName { get; set; }

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

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
                AsyncCallback asyncCallback;
                Object[] syncArguments = SyncToAsyncUtil.BuildSyncArguments(arguments, out asyncCallback);

                MethodInfo syncMethod = SyncToAsyncUtil.GetSyncMethod(method, GetSyncServiceType());
                
                ServiceDescription serviceDescription = SyncToAsyncUtil.CreateServiceDescription(ServiceName, syncMethod, syncArguments);

                IAsyncResult asyncResult = Client.BeginCallProgressableService(serviceDescription, delegate(IAsyncResult ar)
                {
                    try
                    {
                        Object progressedResult = Client.EndCallProgressableService(ar);
                        if (asyncCallback != null)
                        {
                            asyncCallback.Invoke(new AsyncResult(progressedResult));
                        }
                    }
                    catch (Exception e)
                    {
                        if (Log.ErrorEnabled)
                        {
                            Log.Error(e);
                        }
                        throw;
                    }
                }, null);
                return asyncResult;
            }
            ServiceDescription serviceDescription2 = SyncToAsyncUtil.CreateServiceDescription(ServiceName, method, arguments);
            Object result = Service.CallProgressableService(serviceDescription2);
            return result;
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