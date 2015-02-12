namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeListenerExtendable
    {
        void RegisterMergeListener(IMergeListener mergeListener);

        void UnregisterMergeListener(IMergeListener mergeListener);
    }
}