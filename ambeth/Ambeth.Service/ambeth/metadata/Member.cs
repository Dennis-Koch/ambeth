using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public abstract class Member : AbstractAccessor, IComparable<Member>
    {
        protected Member(Type type, IPropertyInfo property)
            : base(type, property)
        {
            // intended blank
        }

        public int CompareTo(Member o)
        {
            return Name.CompareTo(o.Name);
        }

        public abstract Type ElementType { get; }

        public abstract Type DeclaringType { get; }

        public abstract Type RealType { get; }

        public abstract Object NullEquivalentValue { get; }

        public abstract V GetAnnotation<V>() where V : Attribute;

        public abstract String Name { get; }

	    public override bool Equals(Object obj)
	    {
		    if (obj == this)
		    {
			    return true;
		    }
		    if (obj == null || !obj.GetType().Equals(GetType()))
		    {
			    return false;
		    }
		    Member other = (Member) obj;
            return DeclaringType.Equals(other.DeclaringType) && Name.Equals(other.Name);
	    }

	    public override int GetHashCode()
	    {
            return GetType().GetHashCode() ^ DeclaringType.GetHashCode() ^ Name.GetHashCode();
	    }

        public override String ToString()
        {
            return "Member " + Name;
        }
    }
}