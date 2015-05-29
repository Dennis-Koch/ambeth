using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Orm
{
    public interface IEntityConfig
    {
        Type EntityType { get; }

        Type RealType { get; }

        bool Local { get; }

        String TableName { get; }

        String PermissionGroupName { get; }

        String SequenceName { get; }

        IMemberConfig IdMemberConfig { get; }

        IMemberConfig VersionMemberConfig { get; }

		String DescriminatorName { get; }

        bool VersionRequired { get; }

        IMemberConfig CreatedByMemberConfig { get; }

        IMemberConfig CreatedOnMemberConfig { get; }

        IMemberConfig UpdatedByMemberConfig { get; }

        IMemberConfig UpdatedOnMemberConfig { get; }

        IEnumerable<IMemberConfig> GetMemberConfigIterable();

		IEnumerable<IRelationConfig> GetRelationConfigIterable();
    }
}
