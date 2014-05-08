using System;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface IRelationProvider
    {
        bool IsEntityType(Type type);

        String CreatedOnMemberName { get; }

        String CreatedByMemberName { get; }

        String UpdatedOnMemberName { get; }

        String UpdatedByMemberName { get; }

        String VersionMemberName { get; }

        String IdMemberName { get; }
    }
}
