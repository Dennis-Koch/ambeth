using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public abstract class RelationMember : Member
    {
        protected RelationMember(Type type, IPropertyInfo property)
            : base(type, property)
        {
            // intended blank
        }

        public override Object NullEquivalentValue
        {
            get
            {
                return null;
            }
        }

        public abstract CascadeLoadMode CascadeLoadMode { get; }

        public abstract bool IsManyTo { get; }
    }
}