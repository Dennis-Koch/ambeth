using System;

namespace De.Osthus.Ambeth.Propertychange
{
    public interface IPropertyChangeExtendable
    {
	    void RegisterPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension, Type entityType);

	    void UnregisterPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension, Type entityType);
    }
}
