using System;

namespace De.Osthus.Ambeth.Orm
{
    public interface IMemberConfig : IOrmConfig
    {
        bool AlternateId { get; }

        bool Ignore { get; }

		bool Transient { get; }

		String DefinedBy { get; }
    }
}