using System;

namespace De.Osthus.Ambeth.Bytecode.AbstractObject
{
    /**
     * The context for the {@link ImplementAbstractObjectFactory}
     */
    public class ImplementAbstractObjectEnhancementHint : IEnhancementHint
    {
        public static readonly ImplementAbstractObjectEnhancementHint INSTANCE = new ImplementAbstractObjectEnhancementHint();

        public Object Unwrap(Type includedContextType)
        {
            if (typeof(ImplementAbstractObjectEnhancementHint).IsAssignableFrom(includedContextType))
            {
                return this;
            }
            return null;
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }
    }
}
