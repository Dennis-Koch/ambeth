using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Progress.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Progress
{
    public class ProgressDispatcherModule : IInitializingModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<ProgressDispatcher>("progressDispatcher").Autowireable<IProgressDispatcherIntern>();
        }
    }

    public class ProgressDispatcher : IProgressDispatcher, IInitializingBean
    {
        protected readonly List<ProgressItem> progressItems = new List<ProgressItem>();

        public IProgressListener ProgressListener { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ProgressListener, "ProgressListener");
        }

        public bool IsProgressPending
        {
            get
            {
                return progressItems.Count > 0;
            }
        }

        public IProgress Status
        {
            get
            {
                if (progressItems.Count == 0)
                {
                    return null;
                }
                ProgressItem progressItem = progressItems[progressItems.Count - 1];

                ProgressEvent progressEvent = new ProgressEvent();
                progressEvent.ProgressID = progressItem.progressId;
                progressEvent.CurrentSteps = progressItem.stepCount;
                progressEvent.ProgressID = progressItem.maxCount;
                return progressEvent;
            }
        }    

        public void Failure(Exception e)
        {
            if (progressItems.Count == 0)
            {
                throw new NotSupportedException();
            }
            ProgressItem progressItem = progressItems[progressItems.Count - 1];
            progressItems.RemoveAt(progressItems.Count - 1);
            ProgressItemUpdated(progressItem, e);
        }

        public IProgress StartProgress()
        {
            ProgressItem progressItem = new ProgressItem();
            progressItem.stepCount = 0;
            progressItem.maxCount = 1;
            long progressId = 0;
            while (progressId == 0)
            {
                progressId = (long)(new Random().NextDouble() * long.MaxValue);
                foreach (ProgressItem existingProgressItem in progressItems)
                {
                    if (existingProgressItem.progressId == progressId)
                    {
                        progressId = 0;
                        break;
                    }
                }
            }
            progressItem.progressId = progressId;

            progressItems.Add(progressItem);

            ProgressEvent pEvent = new ProgressEvent();
            pEvent.ProgressID = progressId;
            return pEvent;
        }

        protected ProgressItem GetCurrentProgressItem()
        {
            if (progressItems.Count == 0)
            {
                throw new NotSupportedException();
            }
            return progressItems[progressItems.Count - 1];
        }

        protected void ProgressItemUpdated(ProgressItem progressItem)
        {
            ProgressItemUpdated(progressItem, null);
        }

        protected void ProgressItemUpdated(ProgressItem progressItem, Exception e)
        {
            ProgressEvent progressEvent = new ProgressEvent();
            progressEvent.ProgressID = progressItem.progressId;
            progressEvent.CurrentSteps = progressItem.stepCount;
            progressEvent.ProgressID = progressItem.maxCount;
            progressEvent.Exception = e;

            ProgressListener.HandleProgress(progressEvent);
        }

        public void Step()
        {
            Step(1);
        }

        public void Step(int stepCount)
        {
            Step(1, 0);
        }

        public void Step(int stepCount, int maxCount)
        {
            ProgressItem progressItem = GetCurrentProgressItem();
            progressItem.stepCount += stepCount;
            progressItem.maxCount += maxCount;
            ProgressItemUpdated(progressItem);
        }

        public void EndProgress()
        {
            EndProgress(null);
        }

        public void EndProgress(Object result)
        {
            if (progressItems.Count == 0)
            {
                throw new NotSupportedException();
            }
            ProgressItem progressItem = progressItems[progressItems.Count - 1];
            progressItems.RemoveAt(progressItems.Count - 1);
            progressItem.stepCount = progressItem.maxCount;
            ProgressItemUpdated(progressItem);
        }
    }
}