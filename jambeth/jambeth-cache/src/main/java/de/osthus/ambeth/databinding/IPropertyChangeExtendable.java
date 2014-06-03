package de.osthus.ambeth.databinding;

public interface IPropertyChangeExtendable
{
	void registerPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension, Class<?> entityType);

	void unregisterPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension, Class<?> entityType);
}
