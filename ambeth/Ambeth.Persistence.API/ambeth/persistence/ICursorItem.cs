using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ICursorItem : IVersionItem
    {
        Object[] Values { get; }
    }
}