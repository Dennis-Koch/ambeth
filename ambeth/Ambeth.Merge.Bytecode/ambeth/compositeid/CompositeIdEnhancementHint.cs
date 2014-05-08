using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.CompositeId
{
    public class CompositeIdEnhancementHint : IEnhancementHint
    {
        private readonly ITypeInfoItem[] idMembers;

        public CompositeIdEnhancementHint(ITypeInfoItem[] idMembers)
        {
            this.idMembers = idMembers;
        }

        public ITypeInfoItem[] IdMembers
        {
            get
            {
                return idMembers;
            }
        }

        public T Unwrap<T>() where T : IEnhancementHint
        {
            return (T)Unwrap(typeof(T));
        }

        public Object Unwrap(Type includedContextType)
        {
            if (typeof(CompositeIdEnhancementHint).IsAssignableFrom(includedContextType))
            {
                return this;
            }
            return null;
        }

    }
}

