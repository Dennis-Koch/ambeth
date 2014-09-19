using System;
namespace De.Osthus.Ambeth.Merge.Model
{
	public class PostLoadMethodLifecycleExtension : AbstractMethodLifecycleExtension
	{
        public override  void PostCreate(IEntityMetaData metaData, Object newEntity)
        {
            // intended blank
        }

        public override void PostLoad(IEntityMetaData metaData, Object entity)
		{
			CallMethod(entity, "PostLoad");
		}

        public override void PrePersist(IEntityMetaData metaData, Object entity)
		{
			// intended blank
		}
	}
}
