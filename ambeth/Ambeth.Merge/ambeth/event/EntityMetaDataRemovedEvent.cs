using System;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Event
{
    public class EntityMetaDataRemovedEvent : IEntityMetaDataEvent
    {
        protected Type[] entityTypes;

        public Type[] EntityTypes
        {
            get
            {
                return entityTypes;
            }
        }

        public EntityMetaDataRemovedEvent(params Type[] entityTypes)
        {
            this.entityTypes = entityTypes;
        }
    }
}
