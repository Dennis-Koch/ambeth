using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public sealed class EntityEnhancementHint : IEnhancementHint
    {
        public static readonly EntityEnhancementHint HOOK = new EntityEnhancementHint();

        private EntityEnhancementHint()
        {
            // Intended blank
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public Object Unwrap(Type includedContextType)
        {
            if (typeof(EntityEnhancementHint).IsAssignableFrom(includedContextType))
            {
                return this;
            }
            return null;
        }
    }
}
