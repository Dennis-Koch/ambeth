using System;
using System.Reflection;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Threading;
#if !SILVERLIGHT
using Castle.DynamicProxy;
using System.Threading;
#else
using Castle.Core.Interceptor;
using Castle.DynamicProxy;
#endif

namespace De.Osthus.Ambeth.Proxy
{
    abstract public class AbstractInterceptor<S, C>
        : CascadedInterceptor
    {
        public S Service { get; set; }

        public C Client { get; set; }

        public IGuiThreadHelper GuiThreadHelper { get; set; }

        public IThreadPool ThreadPool { get; set; }

        protected Type GetSyncServiceType()
        {
            return typeof(S);
        }

        protected Type GetAsyncServiceType()
        {
            return typeof(C);
        }

        public override void Intercept(IInvocation invocation)
        {
            if (AnnotationUtil.GetAnnotation<IgnoreAttribute>(invocation.Method, false) != null)
            {
                InvokeTarget(invocation);
                return;
            }
            String methodName = invocation.Method.Name.ToLower();
            Boolean? isAsyncBegin = null;
            if (methodName.StartsWith("begin"))
            {
                isAsyncBegin = true;
                methodName = methodName.Substring(5);
            }
            else if (methodName.StartsWith("end"))
            {
                isAsyncBegin = false;
                methodName = methodName.Substring(3);
            }
            if (GuiThreadHelper.IsInGuiThread())
            {
                if (!typeof(void).Equals(invocation.Method.ReturnType))
                {
                    throw new Exception("It is not allowed to call synchronuous methods from GUI thread. Please use '" + typeof(IThreadPool).FullName
                        + "' to make this synchronuous call from a background thread");
                }
                ThreadPool.Queue(delegate()
                {
                    Intercept(invocation, methodName, isAsyncBegin);
                });
                return;
            }
            Object result = Intercept(invocation, methodName, isAsyncBegin);
            invocation.ReturnValue = result;
        }

        protected Object Intercept(IInvocation invocation, String methodName, Boolean? isAsyncBegin)
        {
            if (AnnotationUtil.GetAnnotation<ProcessAttribute>(invocation.Method, false) != null)
            {
                return InterceptApplication(invocation, isAsyncBegin);
            }
            if (AnnotationUtil.GetAnnotation<MergeAttribute>(invocation.Method, false) != null
                || methodName.StartsWith("update")
                || methodName.StartsWith("save")
                || methodName.StartsWith("merge")
                || methodName.StartsWith("insert"))
            {
                return InterceptMerge(invocation, isAsyncBegin);
            }
            if (AnnotationUtil.GetAnnotation<RemoveAttribute>(invocation.Method, false) != null
                || methodName.StartsWith("delete")
                || methodName.StartsWith("remove"))
            {
                return InterceptDelete(invocation, isAsyncBegin);
            }
            if (AnnotationUtil.GetAnnotation<FindAttribute>(invocation.Method, false) != null
                || methodName.StartsWith("retrieve")
                || methodName.StartsWith("read")
                || methodName.StartsWith("find")
                || methodName.StartsWith("get"))
            {
                return InterceptLoad(invocation, isAsyncBegin);
            }
            if (methodName.Equals("close") || methodName.Equals("abort"))
            {
                // Intended blank
            }
            return InterceptApplication(invocation, isAsyncBegin);
        }

        protected virtual Object InterceptApplication(IInvocation invocation,
            Boolean? isAsyncBegin)
        {
            InvokeTarget(invocation);
            return invocation.ReturnValue;
        }

        protected virtual Object InterceptLoad(IInvocation invocation,
            Boolean? isAsyncBegin)
        {
            InvokeTarget(invocation);

            if (isAsyncBegin.HasValue && isAsyncBegin.Value)
            {
                return (IAsyncResult)invocation.ReturnValue;
            }
            return InterceptLoadIntern(invocation.Method, invocation.Arguments, isAsyncBegin, invocation.ReturnValue);
        }

        protected virtual Object InterceptMerge(IInvocation invocation,
            Boolean? isAsyncBegin)
        {
            if (isAsyncBegin.HasValue && !isAsyncBegin.Value)
            {
                return ((IAsyncResult)invocation.Arguments[0]).AsyncState;
            }
            return InterceptMergeIntern(invocation.Method, invocation.Arguments, isAsyncBegin);
        }

        protected virtual Object InterceptDelete(IInvocation invocation,
            Boolean? isAsyncBegin)
        {
            if (isAsyncBegin.HasValue && !isAsyncBegin.Value)
            {
                return ((IAsyncResult)invocation.Arguments[0]).AsyncState;
            }
            return InterceptDeleteIntern(invocation.Method, invocation.Arguments, isAsyncBegin);
        }

        protected virtual Object InterceptLoadIntern(MethodInfo method, Object[] arguments,
            Boolean? isAsyncBegin, Object result)
        {
            return result;
        }

        abstract protected Object InterceptMergeIntern(MethodInfo method, Object[] arguments,
            Boolean? isAsyncBegin);

        abstract protected Object InterceptDeleteIntern(MethodInfo method, Object[] arguments,
            Boolean? isAsyncBegin);
    }
}