using De.Osthus.Ambeth.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Orm
{
	public class OrmConfigGroup : IOrmConfigGroup
	{
		protected readonly ISet<IEntityConfig> localEntityConfigs;

		protected readonly ISet<IEntityConfig> externalEntityConfigs;

		public OrmConfigGroup(ISet<IEntityConfig> localEntityConfigs, ISet<IEntityConfig> externalEntityConfigs)
		{
			this.localEntityConfigs = localEntityConfigs;
			this.externalEntityConfigs = externalEntityConfigs;
		}

		public IEnumerable<IEntityConfig> GetExternalEntityConfigs()
		{
			return externalEntityConfigs;
		}

		public IEnumerable<IEntityConfig> GetLocalEntityConfigs()
		{
			return localEntityConfigs;
		}
	}
}