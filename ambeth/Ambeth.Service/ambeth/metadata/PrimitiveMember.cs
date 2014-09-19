using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public abstract class PrimitiveMember : Member
    {
        protected PrimitiveMember(Type type, IPropertyInfo property)
            : base(type, property)
        {
            // intended blank		
        }

        public abstract bool TechnicalMember { get; }
    }
}
