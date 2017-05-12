namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityContext
    {
        IAuthentication Authentication { get; set; }

        IAuthorization Authorization { get; set; }
    }
}
