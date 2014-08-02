package de.osthus.ambeth.merge;

public interface IEntityFactoryExtensionProvider
{
	IEntityInstantiationExtension getEntityFactoryExtension(Class<?> entityType);
}
