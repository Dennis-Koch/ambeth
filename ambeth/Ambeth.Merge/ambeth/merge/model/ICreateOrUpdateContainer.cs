namespace De.Osthus.Ambeth.Merge.Model
{
    public interface ICreateOrUpdateContainer
    {
        IPrimitiveUpdateItem[] GetFullPUIs();

        IRelationUpdateItem[] GetFullRUIs();
    }
}