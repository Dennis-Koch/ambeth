using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Bytecode
{
    /**
     * Entity based on an interface
     */
    public interface IEntityA : IBaseEntity<IEntityA>
    {
        // intended blank
    }
}
