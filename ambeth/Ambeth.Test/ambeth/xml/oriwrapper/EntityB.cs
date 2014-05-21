using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Test.Model
{
    [XmlType]
    public class EntityB : BaseEntity
    {
        public virtual EntityA EntityA { get; set; }
    }
}
