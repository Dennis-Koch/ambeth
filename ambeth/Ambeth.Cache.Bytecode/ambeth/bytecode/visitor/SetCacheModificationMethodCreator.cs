using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class SetCacheModificationMethodCreator : ClassVisitor
    {
        private static readonly MethodInstance callCacheModification = new MethodInstance(null, typeof(SetCacheModificationMethodCreator), "CallCacheModification",
                typeof(ICacheModification), typeof(bool), typeof(bool));

        private static readonly String cacheModificationName = "f_cacheModification";

        public static PropertyInstance GetCacheModificationPI(IClassVisitor cv)
	    {
		    PropertyInstance pi = State.GetProperty(cacheModificationName);
		    if (pi != null)
		    {
			    return pi;
		    }
		    Object bean = State.BeanContext.GetService<ICacheModification>();
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
            mg.InvokeInterface(new MethodInstance(null, typeof(ICacheModification), "get_Active"));
            mg.StoreLocal(loc_oldActive);

            // callModificationActive(cacheModification, oldActive, true)
            mg.LoadLocal(loc_cacheModification);
            mg.LoadLocal(loc_oldActive);
            mg.Push(true);
            mg.InvokeStatic(callCacheModification);

            mg.TryFinally(script, delegate(IMethodVisitor mv2)
                {
                    // callModificationActive(cacheModification, oldActive, false)
                    mv2.LoadLocal(loc_cacheModification);
                    mv2.LoadLocal(loc_oldActive);
                    mv2.Push(false);
                    mv2.InvokeStatic(callCacheModification);
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

        public static void CallCacheModification(ICacheModification cacheModification, bool oldValue, bool newValue)
        {
            if (!oldValue)
            {
                cacheModification.Active = newValue;
            }
        }
    }
}