using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Collections
{
    public interface ISetEntry<K> : IPrintable
    {
        K Key { get; }

        int Hash { get; }

        ISetEntry<K> NextEntry { get; }
    }
}