using System;
using System.Collections;
using System.Reflection;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
using Castle.DynamicProxy;
#endif
using System.Runtime.ExceptionServices;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Log.Interceptor
{
    public class LogInterceptor : CascadedInterceptor, IInitializingBean
    {
        public static LogInterceptor Create(bool printShortStringNames)
        {
            return Create(printShortStringNames, false);
        }

        public static LogInterceptor Create(bool printShortStringNames, bool isServerLogger)
        {
            LogInterceptor interceptor = new LogInterceptor();
            interceptor.PrintShortStringNames = printShortStringNames;
            interceptor.IsClientLogger = !isServerLogger;
            return interceptor;
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        public IProperties Properties { protected get; set; }

        public IExceptionHandler ExceptionHandler { protected get; set; }

        [Property(ServiceConfigurationConstants.LogShortNames, DefaultValue = "true")]
        public bool PrintShortStringNames { protected get; set; }

        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public bool IsClientLogger { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(Properties, "Properties");
        }

        [HandleProcessCorruptedStateExceptions]
        protected override void InterceptIntern(IInvocation invocation)
        {
            try
            {
                int startTicks = 0;
                ILogger loggerOfMethod = LoggerFactory.GetLogger(invocation.Method.DeclaringType, Properties);
                bool debugEnabled = Log.DebugEnabled && loggerOfMethod.DebugEnabled;
                if (debugEnabled)
                {
                    if (IsClientLogger)
                    {
                        loggerOfMethod.Debug("Start:     " + LogTypesUtil.PrintMethod(invocation.Method, PrintShortStringNames));
                    }
                    else
                    {
                        loggerOfMethod.Debug("Start(S):  " + LogTypesUtil.PrintMethod(invocation.Method, PrintShortStringNames));
                    }
                    startTicks = Environment.TickCount;
                }
                InvokeTarget(invocation);
                if (debugEnabled)
                {
                    int endTicks = Environment.TickCount;
                    Object returnValue = invocation.ReturnValue;
                    int resultCount = returnValue is ICollection ? ((ICollection)returnValue).Count : returnValue != null ? 1 : -1;
                    String resultString = resultCount >= 0 ? "" + resultCount : "no";
                    String itemsString = typeof(void).Equals(invocation.Method.ReturnType) ? "" : " with " + resultString + (resultCount != 1 ? " items" : " item");
                    if (IsClientLogger)
                    {
                        loggerOfMethod.Debug("Finish:    " + LogTypesUtil.PrintMethod(invocation.Method, PrintShortStringNames) + itemsString + " (" + (endTicks - startTicks) + " ms)");
                    }
                    else
                    {
                        loggerOfMethod.Debug("Finish(S): " + LogTypesUtil.PrintMethod(invocation.Method, PrintShortStringNames) + itemsString + " (" + (endTicks - startTicks) + " ms)");
                    }
                }
            }
            catch (TargetInvocationException e)
            {
                Exception ex = e.InnerException;
                if (ExceptionHandler != null)
                {
                    ex = ExceptionHandler.HandleException(invocation.Method, ex);
                }
                if (Log.ErrorEnabled)
                {
                    Log.Error(ex);
                }
                throw new RethrownException(ex);
            }
            catch (Exception e)
            {
                if (Log.ErrorEnabled)
                {
                    Log.Error(e);
                }
                throw;
            }
        }

        public override string ToString()
        {
            return GetType().Name + ": " + Target;
        }
    }
}