using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ILinkCursorItem
    {
        Object FromId { get; }

        Object ToId { get; }
    }
}
