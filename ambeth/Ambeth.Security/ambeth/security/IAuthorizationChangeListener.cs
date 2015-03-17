namespace De.Osthus.Ambeth.Security
{
    public interface IAuthorizationChangeListener
    {
        void AuthorizationChanged(IAuthorization authorization);
    }
}