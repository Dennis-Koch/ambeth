using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public interface IBaseEntity<T>
    {
        long? Id { get; }

        String Name { get; set; }

        String Value { get; set; }
    }
}
