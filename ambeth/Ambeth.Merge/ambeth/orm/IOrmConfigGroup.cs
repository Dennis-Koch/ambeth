using System.Collections.Generic;

namespace De.Osthus.Ambeth.Orm
{
	public interface IOrmConfigGroup
	{
		IEnumerable<IEntityConfig> GetLocalEntityConfigs();

		IEnumerable<IEntityConfig> GetExternalEntityConfigs();
	}
}
