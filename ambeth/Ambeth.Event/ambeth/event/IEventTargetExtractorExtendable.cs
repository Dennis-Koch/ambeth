using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventTargetExtractorExtendable
    {
        void RegisterEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Type eventType);

        void UnregisterEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Type eventType);
    }
}
