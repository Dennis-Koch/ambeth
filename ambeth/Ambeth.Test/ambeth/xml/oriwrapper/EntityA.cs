using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Test.Model
{
    [XmlType]
    public class EntityA : BaseEntity
    {
        public virtual IList<EntityB> EntityBs { get; set; }
    }
}
