namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityScopeChangeListenerExtendable
    {
        void RegisterSecurityScopeChangeListener(ISecurityScopeChangeListener securityScopeChangeListener);

        void UnregisterSecurityScopeChangeListener(ISecurityScopeChangeListener securityScopeChangeListener);
    }
}