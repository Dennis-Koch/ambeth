﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Event.Config
{
    public class EventConfigurationConstants
    {
        public const String PollingActive = "event.polling.active";

        public const String StartPausedActive = "event.polling.paused.on.start.active";

        public const String PollingSleepInterval = "event.polling.sleepinterval";

        public const String MaxWaitInterval = "event.polling.maxwaitinterval";

        public const String EventServiceBeanActive = "event.service.active";
    }
}
