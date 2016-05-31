package de.osthus.ambeth.mapping;

public interface IPropertyExpansionExtendable
{
	void registerEntityExpansionExtension(PropertyPath expansionPath, Class<?> transferClass, String propertyName);

	void unregisterEntityExpansionExtension(PropertyPath expansionPath, Class<?> transferClass, String propertyName);
}
