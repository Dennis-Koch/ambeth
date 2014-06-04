using System;

namespace De.Osthus.Ambeth.Databinding
{
    public interface IPropertyChangeExtendable
    {
        void RegisterPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension, Type entityType);

        void UnregisterPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension, Type entityType);
    }
}