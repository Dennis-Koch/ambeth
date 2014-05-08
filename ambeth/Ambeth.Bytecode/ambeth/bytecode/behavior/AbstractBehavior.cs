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

        //protected ClassNode readClassNode(Type type)
        //{
        //    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //    return readClassNode(classLoader.getResourceAsStream(NewType.getInternalName(type) + ".class"));
        //}

        //protected ClassNode readClassNode(InputStream is)
        //{
        //    try
        //    {
        //        ClassReader cr = new ClassReader(is);
        //        ClassNode cn = new ClassNode();
        //        cr.accept(cn, 0);
        //        return cn;
        //    }
        //    catch (Throwable e)
        //    {
        //        throw RuntimeExceptionUtil.mask(e);
        //    }
        //    finally
        //    {
        //        try
        //        {
        //            is.close();
        //        }
        //        catch (IOException e)
        //        {
        //            // Intended blank
        //        }
        //    }
        //}

        protected bool IsAnnotationPresent<V>(Type type) where V : Attribute
        {
            if (type == null)
            {
                return false;
            }
            if (AnnotationUtil.IsAnnotationPresent<V>(type, false))
            {
                return true;
            }
            return IsAnnotationPresent<V>(type.BaseType);
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