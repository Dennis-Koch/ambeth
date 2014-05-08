using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Typeinfo
{
    [XmlType]
    public interface IRelationInfoItem : ITypeInfoItem
    {
        CascadeLoadMode CascadeLoadMode { get; }

        bool IsToMany { get; }

        bool IsManyTo { get; }
    }
}
