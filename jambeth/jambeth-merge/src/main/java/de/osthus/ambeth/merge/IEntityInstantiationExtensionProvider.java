package de.osthus.ambeth.merge;

public interface IEntityInstantiationExtensionProvider
{
	IEntityInstantiationExtension getEntityInstantiationExtension(Class<?> entityType);
}
