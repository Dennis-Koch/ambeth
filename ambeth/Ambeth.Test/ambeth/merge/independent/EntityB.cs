using System;
namespace De.Osthus.Ambeth.Merge.Independent
{
    public class EntityB : BaseEntity
    {
        public String Name { get; set; }

        public EntityA A { get; set; }
    }
}