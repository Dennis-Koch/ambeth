using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security
{
    public interface ISecurityScopeChangeListener
    {
        void SecurityScopeChanged(ISecurityScope[] securityScopes);
    }
}