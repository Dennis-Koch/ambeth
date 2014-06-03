using System;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Event
{
    public class EntityMetaDataAddedEvent : IEntityMetaDataEvent
    {
        protected Type[] entityTypes;

        public Type[] EntityTypes
        {
            get
            {
                return entityTypes;
            }
        }

        public EntityMetaDataAddedEvent(params Type[] entityTypes)
        {
            this.entityTypes = entityTypes;
        }
    }
}
