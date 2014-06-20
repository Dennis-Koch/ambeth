using System.Collections.Generic;

namespace De.Osthus.Ambeth.Merge.Independent
{
    public class EntityA : BaseEntity
    {
        public EntityA Other { get; set; }

        public IList<EntityB> Bs { get; set; }

        public Embedded Embedded { get; set; }
    }
}