using System;
namespace De.Osthus.Ambeth.Merge.Model
{
    public class PrePersistMethodLifecycleExtension : AbstractMethodLifecycleExtension
    {
	    public override void PostLoad(Object entity)
	    {
		    // intended blank
	    }

        public override void PrePersist(Object entity)
	    {
		    CallMethod(entity, "PrePersist");
	    }
    }
}
