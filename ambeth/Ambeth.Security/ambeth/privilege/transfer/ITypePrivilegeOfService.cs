using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using System;

namespace De.Osthus.Ambeth.Privilege.Transfer
{
    [XmlType]
    public interface ITypePrivilegeOfService
    {
        Type EntityType { get; }

        ISecurityScope SecurityScope { get; }

        bool? ReadAllowed { get; }

        bool? CreateAllowed { get; }

        bool? UpdateAllowed { get; }

        bool? DeleteAllowed { get; }

        bool? ExecuteAllowed { get; }

        String[] PropertyPrivilegeNames { get; }

        ITypePropertyPrivilegeOfService[] PropertyPrivileges { get; }
    }
}
