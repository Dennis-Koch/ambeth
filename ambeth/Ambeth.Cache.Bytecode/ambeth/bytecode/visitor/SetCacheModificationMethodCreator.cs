using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class SetCacheModificationMethodCreator : ClassVisitor
    {
        private static readonly MethodInstance m_callCacheModificationActive = new MethodInstance(null, typeof(SetCacheModificationMethodCreator), typeof(void), "CallCacheModificationActive",
                typeof(ICacheModification), typeof(bool), typeof(bool));

        private static readonly MethodInstance m_callCacheModificationInternalUpdate = new MethodInstance(null, typeof(SetCacheModificationMethodCreator), typeof(void), "CallCacheModificationInternalUpdate",
        typeof(ICacheModification), typeof(bool), typeof(bool));

        private static readonly String cacheModificationName = "__CacheModification";

        public static PropertyInstance GetCacheModificationPI(IClassVisitor cv)
	    {
            Object bean = State.BeanContext.GetService<ICacheModification>();
            PropertyInstance pi = State.GetProperty(cacheModificationName, NewType.GetType(bean.GetType()));
		    if (pi != null)
		    {
			    return pi;
		    }
		    return cv.ImplementAssignedReadonlyProperty(cacheModificationName, bean);
	    }

        public static void CacheModificationActive(PropertyInstance p_cacheModification, IMethodVisitor mg, Script script)
        {
            LocalVariableInfo loc_cacheModification = mg.NewLocal<ICacheModification>();
            LocalVariableInfo loc_oldActive = mg.NewLocal<bool>();

            // ICacheModification cacheModification = this.cacheModification;
            mg.CallThisGetter(p_cacheModification);
            mg.StoreLocal(loc_cacheModification);

            // boolean oldActive = cacheModification.isActive();
            mg.LoadLocal(loc_cacheModification);
            mg.InvokeInterface(new MethodInstance(null, typeof(ICacheModification), typeof(bool), "get_Active"));
            mg.StoreLocal(loc_oldActive);

            // callModificationActive(cacheModification, oldActive, true)
            mg.LoadLocal(loc_cacheModification);
            mg.LoadLocal(loc_oldActive);
            mg.Push(true);
            mg.InvokeStatic(m_callCacheModificationActive);

            mg.TryFinally(script, delegate(IMethodVisitor mv2)
                {
                    // callModificationActive(cacheModification, oldActive, false)
                    mv2.LoadLocal(loc_cacheModification);
                    mv2.LoadLocal(loc_oldActive);
                    mv2.Push(false);
                    mv2.InvokeStatic(m_callCacheModificationActive);
                });
        }

        public static void CacheModificationInternalUpdate(PropertyInstance p_cacheModification, IMethodVisitor mg, Script script)
        {
            LocalVariableInfo loc_cacheModification = mg.NewLocal<ICacheModification>();
            LocalVariableInfo loc_oldActive = mg.NewLocal<bool>();

            // ICacheModification cacheModification = this.cacheModification;
            mg.CallThisGetter(p_cacheModification);
            mg.StoreLocal(loc_cacheModification);

            // boolean oldInternalUpdate = cacheModification.isInternalUpdate();
            mg.LoadLocal(loc_cacheModification);
            mg.InvokeInterface(new MethodInstance(null, typeof(ICacheModification), typeof(bool), "get_InternalUpdate"));
            mg.StoreLocal(loc_oldActive);

            // callModificationInternalUpdate(cacheModification, oldInternalUpdate, true)
            mg.LoadLocal(loc_cacheModification);
            mg.LoadLocal(loc_oldActive);
            mg.Push(true);
            mg.InvokeStatic(m_callCacheModificationInternalUpdate);

            mg.TryFinally(script, delegate(IMethodVisitor mv2)
            {
                // callModificationInternalUpdate(cacheModification, oldInternalUpdate, false)
                mv2.LoadLocal(loc_cacheModification);
                mv2.LoadLocal(loc_oldActive);
                mv2.Push(false);
                mv2.InvokeStatic(m_callCacheModificationInternalUpdate);
            });
        }

        public SetCacheModificationMethodCreator(IClassVisitor cv)
            : base(cv)
        {
            // Intended blank
        }

        public override void VisitEnd()
        {
            // force implementation
            GetCacheModificationPI(this);

            base.VisitEnd();
        }

        public static void CallCacheModificationActive(ICacheModification cacheModification, bool oldValue, bool newValue)
        {
            if (!oldValue)
            {
                cacheModification.Active = newValue;
            }
        }

        public static void CallCacheModificationInternalUpdate(ICacheModification cacheModification, bool oldValue, bool newValue)
        {
            if (!oldValue)
            {
                cacheModification.InternalUpdate = newValue;
            }
        }
    }
}