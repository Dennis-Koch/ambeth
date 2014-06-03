using System;
namespace De.Osthus.Ambeth.Merge.Model
{
	public class PostLoadMethodLifecycleExtension : AbstractMethodLifecycleExtension
	{
		public override void PostLoad(Object entity)
		{
			CallMethod(entity, "PostLoad");
		}

		public override void PrePersist(Object entity)
		{
			// intended blank
		}
	}
}
