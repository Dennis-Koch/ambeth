using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Progress.Model;

namespace De.Osthus.Ambeth.Progress
{
    public class NoOpProgressDispatcher : IProgressDispatcherIntern
    {
        public bool IsProgressPending
        {
            get
            {
                return false;
            }
        }

        public void Failure(Exception e)
        {
            // Intended blank
        }

        public IProgress StartProgress()
        {
            return null;
            // Intended blank
        }

        public void Step()
        {
            // Intended blank
        }

        public void Step(int stepCount)
        {
            // Intended blank
        }

        public void Step(int stepCount, int maxCount)
        {
            // Intended blank
        }

        public void EndProgress()
        {
            // Intended blank
        }

        public void EndProgress(Object result)
        {
            // Intended blank
        }
    }
}