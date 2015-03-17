using System;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IPropertyChangeConfigurable
    {
        bool Is__PropertyChangeActive();

        void Set__PropertyChangeActive(bool active);
    }
}
