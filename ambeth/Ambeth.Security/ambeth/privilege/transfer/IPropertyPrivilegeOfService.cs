using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Privilege.Transfer
{
    [XmlType]
    public interface IPropertyPrivilegeOfService
    {
        bool ReadAllowed { get; }

        bool CreateAllowed { get; }

        bool UpdateAllowed { get; }

        bool DeleteAllowed { get; }
    }
}