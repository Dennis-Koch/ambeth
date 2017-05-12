namespace De.Osthus.Ambeth.Security
{
    public interface IAuthorizationChangeListenerExtendable
    {
        void RegisterAuthorizationChangeListener(IAuthorizationChangeListener authorizationChangeListener);

        void UnregisterAuthorizationChangeListener(IAuthorizationChangeListener authorizationChangeListener);
    }
}