using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface IEntityMetaDataRefresher
    {
        void RefreshMembers(IEntityMetaData metaData);
    }
}
