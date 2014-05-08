using System.Collections.Generic;
using System.Collections;
using De.Osthus.Ambeth.Event;
using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Progress;
using De.Osthus.Ambeth.Progress.Model;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Datachange
{
    public class ProgressToEventDispatcher : IProgressListener, IInitializingBean
    {
        public IEventDispatcher EventDispatcher { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(EventDispatcher, "EventDispatcher");
        }

        public void HandleProgress(IProgress progress)
        {
            EventDispatcher.DispatchEvent(progress);
        }
    }
}
