namespace De.Osthus.Ambeth.Persistence
{
    /**
     * Interface to allow registering a hook which gets called after a IDatabase instance is disposed
     * 
     * @author dennis.koch
     *
     */
    public interface IDatabaseDisposeHookExtendable
    {
        void RegisterDisposeHook(IDatabaseDisposeHook disposeHook);

        void UnregisterDisposeHook(IDatabaseDisposeHook disposeHook);
    }
}
