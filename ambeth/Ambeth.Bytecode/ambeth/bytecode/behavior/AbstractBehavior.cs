using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public abstract class AbstractBehavior : IBytecodeBehavior
    {
        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        protected NewType GetDeclaringType(MemberInfo member, NewType newEntityType)
        {
            if (member.DeclaringType.IsInterface)
            {
                return newEntityType;
            }
            return NewType.GetType(member.DeclaringType);
        }

        protected bool IsAnnotationPresent<V>(Type type) where V : Attribute
        {
            if (type == null)
            {
                return false;
            }
            if (IsAnnotationPresentIntern<V>(type))
		    {
			    return true;
		    }
		    Type[] interfaces = type.GetInterfaces();
		    foreach (Type interfaceType in interfaces)
		    {
			    if (IsAnnotationPresent<V>(interfaceType))
			    {
				    return true;
			    }
		    }
		    return false;
	    }

	    protected bool IsAnnotationPresentIntern<V>(Type type) where V : Attribute
	    {
		    if (type == null)
		    {
			    return false;
		    }
		    if (AnnotationUtil.IsAnnotationPresent<V>(type, false))
		    {
			    return true;
		    }
            return IsAnnotationPresentIntern<V>(type.BaseType);
	    }

        public virtual Type[] GetEnhancements()
        {
            return new Type[0];
        }

        public abstract IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                IList<IBytecodeBehavior> cascadePendingBehaviors);

        public virtual Type GetTypeToExtendFrom(Type originalType, Type currentType, IEnhancementHint hint)
        {
            return null;
        }
    }
}