using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Minerva.Security
{
    public class SecurityScopeProvider : ISecurityScopeProvider, ISecurityScopeChangeExtendable, IInitializingBean
    {
        protected ISecurityScope[] securityScope;

        protected readonly IExtendableContainer<ISecurityScopeChangeListener> securityScopeChangeListeners = new DefaultExtendableContainer<ISecurityScopeChangeListener>("securityScopeChangeListener");

        public virtual void AfterPropertiesSet()
        {
            // Intended blank
        }

        public ISecurityScope[] CurrentSecurityScope
        {
            get
            {
                return securityScope;
            }
            set
            {
                securityScope = value;
                ISecurityScopeChangeListener[] listeners = securityScopeChangeListeners.GetExtensions();
                for (int a = 0, size = listeners.Length; a < size; a++)
                {
                    listeners[a].SecurityScopeChanged(value);
                }
            }
        }

        public void registerSecurityScopeChangeListener(ISecurityScopeChangeListener securityScopeChangeListener)
        {
            securityScopeChangeListeners.Register(securityScopeChangeListener);
        }

        public void unregisterSecurityScopeChangeListener(ISecurityScopeChangeListener securityScopeChangeListener)
        {
            securityScopeChangeListeners.Unregister(securityScopeChangeListener);
        }
    }
}
