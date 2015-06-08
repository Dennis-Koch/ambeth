using System;

namespace De.Osthus.Ambeth.Orm
{
	public interface IOrmConfigGroupProvider
	{
		IOrmConfigGroup GetOrmConfigGroup(String xmlFileNames);
	}
}