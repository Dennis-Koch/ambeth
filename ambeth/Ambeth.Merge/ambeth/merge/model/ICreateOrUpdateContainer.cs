namespace De.Osthus.Ambeth.Merge.Model
{
    public interface ICreateOrUpdateContainer : IChangeContainer
    {
        IPrimitiveUpdateItem[] GetFullPUIs();

        IRelationUpdateItem[] GetFullRUIs();
    }
}