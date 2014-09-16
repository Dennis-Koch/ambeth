using System;
using System.Reflection;
using System.Threading;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Proxy;

namespace De.Osthus.Ambeth.Service.Interceptor
{
    public class SyncCallInterceptor : AbstractSimpleInterceptor, IInitializingBean
    {
        public Object AsyncService { get; set; }

        public Type AsyncServiceInterface { get; set; }

        public IOfflineListenerExtendable OfflineListenerExtendable { get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(OfflineListenerExtendable, "OfflineListenerExtendable");
        }

        protected override void InterceptIntern(IInvocation invocation)
        {
            ParameterInfo[] parameters = invocation.Method.GetParameters();
            Object[] arguments = invocation.Arguments;
            MethodInfo method = invocation.Method;

            MethodInfo[] asyncMethods = SyncToAsyncUtil.GetAsyncMethods(method, AsyncServiceInterface);
            MethodInfo beginMethod = asyncMethods[0];
            MethodInfo endMethod = asyncMethods[1];

            if (AsyncService is IInterceptor)
            {
				//TODO: review this code. fail silently?
				// AsyncService must not be the Interceptor, but the proxy object
                return;
            }
            SyncCallItem syncCallItem = new SyncCallItem();
            syncCallItem.ServiceObject = AsyncService;
            syncCallItem.AsyncEndMethod = endMethod;

            Delegate mySyncDelegate = Delegate.CreateDelegate(typeof(AsyncCallback), syncCallItem, "CallbackMethod");

            Object[] beginArguments = new Object[beginMethod.GetParameters().Length];
            Array.Copy(arguments, 0, beginArguments, 0, arguments.Length);

            beginArguments[beginArguments.Length - 2] = mySyncDelegate;
            beginArguments[beginArguments.Length - 1] = null;

            OfflineListenerExtendable.AddOfflineListener(syncCallItem);
            try
            {
                beginMethod.Invoke(AsyncService, beginArguments);

                invocation.ReturnValue = syncCallItem.GetResult();
            }
            finally
            {
                OfflineListenerExtendable.RemoveOfflineListener(syncCallItem);
            }
        }
    }

    public class SyncCallItem : IOfflineListener
    {
        public MethodInfo AsyncEndMethod { get; set; }

        public Object ServiceObject { get; set; }

        public Object Result { get; private set; }

        public Exception ExceptionResult { get; private set; }

        public bool IsResultSet { get; private set; }

        protected Object resultSyncObject = new Object();

        public void CallbackMethod(IAsyncResult asyncResult)
        {
            Object resultValue = null;
            Exception exceptionValue = null;
            try
            {
                resultValue = AsyncEndMethod.Invoke(ServiceObject, new Object[] { asyncResult });
            }
            catch (Exception e)
            {
                exceptionValue = e;
            }
            Monitor.Enter(resultSyncObject);
            try
            {
                Result = resultValue;
                ExceptionResult = exceptionValue;
                IsResultSet = true;
                Monitor.Pulse(resultSyncObject);
            }
            finally
            {
                Monitor.Exit(resultSyncObject);
            }
        }

        public Object GetResult()
        {
            Monitor.Enter(resultSyncObject);
            try
            {
                while (!IsResultSet)
                {
                    Monitor.Wait(resultSyncObject);
                }
                if (ExceptionResult != null)
                {
                    throw new Exception("Error occured while processing asynchronuous call", ExceptionResult);
                }
                return Result;
            }
            finally
            {
                Monitor.Exit(resultSyncObject);
            }
        }

        public void BeginOnline()
        {
            // Intended blank
        }

        public void HandleOnline()
        {
            // Intended blank
        }

        public void EndOnline()
        {
            Monitor.Enter(resultSyncObject);
            try
            {
                ExceptionResult = new ReconnectException();
                IsResultSet = true;
                Monitor.Pulse(resultSyncObject);
            }
            finally
            {
                Monitor.Exit(resultSyncObject);
            }
        }

        public void BeginOffline()
        {
            // Intended blank
        }

        public void HandleOffline()
        {
            // Intended blank
        }

        public void EndOffline()
        {
            Monitor.Enter(resultSyncObject);
            try
            {
                ExceptionResult = new ReconnectException();
                IsResultSet = true;
                Monitor.Pulse(resultSyncObject);
            }
            finally
            {
                Monitor.Exit(resultSyncObject);
            }
        }
    }
}
