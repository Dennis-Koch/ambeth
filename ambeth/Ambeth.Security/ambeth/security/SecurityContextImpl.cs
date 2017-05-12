namespace De.Osthus.Ambeth.Security
{
    public class SecurityContextImpl : ISecurityContext
    {
        protected IAuthentication authentication;

        protected IAuthorization authorization;

        protected readonly SecurityContextHolder securityContextHolder;

        public SecurityContextImpl(SecurityContextHolder securityContextHolder)
        {
            this.securityContextHolder = securityContextHolder;
        }

        public SecurityContextHolder SecurityContextHolder
        {
            get
            {
                return securityContextHolder;
            }
        }

        public IAuthentication Authentication
        {
            get
            {
                return authentication;
            }
            set
            {
                authentication = value;
            }
        }

        public IAuthorization Authorization
        {
            get
            {
                return authorization;
            }
            set
            {
                authorization = value;
                securityContextHolder.NotifyAuthorizationChangeListeners(authorization);
            }
        }
    }
}