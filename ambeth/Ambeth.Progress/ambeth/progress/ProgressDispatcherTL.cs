using System;
using System.Collections.Generic;
using System.Threading;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Progress.Model;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Hierarchy;

namespace De.Osthus.Ambeth.Progress
{
    public class ProgressDispatcherTL : IProgressDispatcherIntern, IInitializingBean
    {
        protected readonly ThreadLocal<IBeanContextHolder<IProgressDispatcherIntern>> progressDispatcherTL = new ThreadLocal<IBeanContextHolder<IProgressDispatcherIntern>>();

        protected readonly NoOpProgressDispatcher noOpProgressDispatcher = new NoOpProgressDispatcher();

        public IServiceContext BeanContext { protected get; set; }

        public void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
        }

        public bool IsProgressPending
        {
            get
            {
                return GetCurrentProgressDispatcher().IsProgressPending;
            }
        }

        public void Failure(Exception e)
        {
            IProgressDispatcherIntern progressDispatcher = GetCurrentProgressDispatcher();
            progressDispatcher.Failure(e);
            if (progressDispatcher.IsProgressPending)
            {
                return;
            }
            IBeanContextHolder<IProgressDispatcherIntern> childSP = progressDispatcherTL.Value;
            progressDispatcherTL.Value = null;
            childSP.Dispose();
        }

        public IProgress StartProgress()
        {
            IBeanContextHolder<IProgressDispatcherIntern> childSP = progressDispatcherTL.Value;
            if (childSP == null)
            {
                IBeanContextHolder<IProgressDispatcherIntern> progressDispatcher = BeanContext.CreateService<IProgressDispatcherIntern>(typeof(ProgressDispatcherModule));
                progressDispatcherTL.Value = progressDispatcher;
            }
            return ((ProgressDispatcher)childSP.GetTypedValue()).StartProgress();
        }

        protected IProgressDispatcherIntern GetCurrentProgressDispatcher()
        {
            IBeanContextHolder<IProgressDispatcherIntern> childSP = progressDispatcherTL.Value;
            if (childSP == null)
            {
                return noOpProgressDispatcher;
            }
            return childSP.GetTypedValue();
        }

        public void Step()
        {
            GetCurrentProgressDispatcher().Step();
        }

        public void Step(int stepCount)
        {
            GetCurrentProgressDispatcher().Step(stepCount);
        }

        public void Step(int stepCount, int maxCount)
        {
            GetCurrentProgressDispatcher().Step(stepCount, maxCount);
        }

        public void EndProgress()
        {
            EndProgress(null);
        }

        public void EndProgress(Object result)
        {
            IProgressDispatcherIntern progressDispatcher = GetCurrentProgressDispatcher();
            progressDispatcher.EndProgress(result);
            if (progressDispatcher.IsProgressPending)
            {
                return;
            }
            IBeanContextHolder<IProgressDispatcherIntern> childSP = progressDispatcherTL.Value;
            progressDispatcherTL.Value = null;
            childSP.Dispose();
        }
    }
}