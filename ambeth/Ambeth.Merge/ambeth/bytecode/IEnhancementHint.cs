using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public interface IEnhancementHint
    {
        T Unwrap<T>() where T : IEnhancementHint;

        Object Unwrap(Type includedHintType);
    }
}
