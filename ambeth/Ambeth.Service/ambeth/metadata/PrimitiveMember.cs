using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public abstract class PrimitiveMember : Member
    {
        public abstract bool TechnicalMember { get; }

		public abstract bool Transient { get; }
    }
}
