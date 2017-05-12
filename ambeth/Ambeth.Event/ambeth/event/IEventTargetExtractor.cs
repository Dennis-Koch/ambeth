using System;
using System.Net;

namespace De.Osthus.Ambeth.Event
{
    public interface IEventTargetExtractor
    {
        Object ExtractEventTarget(Object eventTarget);
    }
}
