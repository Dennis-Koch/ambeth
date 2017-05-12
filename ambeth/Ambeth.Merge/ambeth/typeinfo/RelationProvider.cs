using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Extendable;

namespace De.Osthus.Ambeth.Typeinfo
{
	public class RelationProvider : IRelationProvider, INoEntityTypeExtendable
    {
		protected readonly SmartCopySet<Type> primitiveTypes = new SmartCopySet<Type>();

		protected readonly ClassExtendableContainer<bool> noEntityTypeExtendables = new ClassExtendableContainer<bool>("flag", "noEntityType");

	    public RelationProvider()
	    {
			ImmutableTypeSet.AddImmutableTypesTo(primitiveTypes);

		    primitiveTypes.Add(typeof(Object));
            primitiveTypes.Add(typeof(DateTime));
            primitiveTypes.Add(typeof(TimeSpan));
            primitiveTypes.Add(typeof(Type));
        }

        public virtual bool IsEntityType(Type type)
        {
			if (type == null || type.IsPrimitive || type.IsEnum || primitiveTypes.Contains(type) || noEntityTypeExtendables.GetExtension(type))
            {
                return false;
            }
            if (AnnotationUtil.IsAnnotationPresent<Embeddable>(type, false) || typeof(IImmutableType).IsAssignableFrom(type))
		    {
			    return false;
		    }
            return true;
        }

		public void RegisterNoEntityType(Type noEntityType)
		{
			noEntityTypeExtendables.Register(true, noEntityType);
		}

		public void UnregisterNoEntityType(Type noEntityType)
		{
			noEntityTypeExtendables.Unregister(true, noEntityType);
		}
    }
}
