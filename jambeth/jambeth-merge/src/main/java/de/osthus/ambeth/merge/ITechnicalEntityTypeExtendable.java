package de.osthus.ambeth.merge;

public interface ITechnicalEntityTypeExtendable
{
	void registerTechnicalEntityType(Class<?> technicalEntityType, Class<?> entityType);

	void unregisterTechnicalEntityType(Class<?> technicalEntityType, Class<?> entityType);
}
