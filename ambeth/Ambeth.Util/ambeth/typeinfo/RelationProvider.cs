using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class RelationProvider : IRelationProvider
    {
       	protected static readonly ISet<Type> primitiveTypes = new HashSet<Type>();

	    static RelationProvider()
	    {
		    primitiveTypes.Add(typeof(Object));
            primitiveTypes.Add(typeof(DateTime));
            primitiveTypes.Add(typeof(TimeSpan));
            primitiveTypes.Add(typeof(Type));
        }

        public virtual bool IsEntityType(Type type)
        {
            if (type == null || ImmutableTypeSet.IsImmutableType(type) || primitiveTypes.Contains(type) || type.IsEnum)
            {
                return false;
            }
            return true;
        }

        public virtual String CreatedOnMemberName
        {
            get
            {
                return null;
            }
        }

        public virtual String CreatedByMemberName
        {
            get
            {
                return null;
            }
        }

        public virtual String UpdatedOnMemberName
        {
            get
            {
                return null;
            }
        }

        public virtual String UpdatedByMemberName
        {
            get
            {
                return null;
            }
        }

        public virtual String VersionMemberName
        {
            get
            {
                return null;
            }
        }

        public virtual String IdMemberName
        {
            get
            {
                return null;
            }
        }
    }
}
