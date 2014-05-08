using System;

namespace De.Osthus.Ambeth.Bytecode.AbstractObject
{
    /**
     * The context for the {@link ImplementAbstractObjectFactory}
     */
    public class ImplementAbstractObjectContext : IEnhancementHint
    {
        public static readonly ImplementAbstractObjectContext INSTANCE;

        public Object Unwrap(Type includedContextType)
        {
            if (typeof(ImplementAbstractObjectContext).IsAssignableFrom(includedContextType))
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
