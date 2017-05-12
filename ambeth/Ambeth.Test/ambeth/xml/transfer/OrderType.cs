using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Xml.Test.Transfer
{
    [XmlType(Name = "OrderType", Namespace = "Comtrack")]
    public enum OrderType
    {
        FTE
    }
}