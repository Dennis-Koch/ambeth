using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Orm;

namespace De.Osthus.Ambeth.Merge.Config
{
    public interface IEntityMetaDataReader
    {
		void AddMembers(EntityMetaData metaData, IEntityConfig entityConfig);
    }
}