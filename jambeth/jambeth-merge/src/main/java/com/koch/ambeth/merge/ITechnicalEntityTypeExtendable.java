package com.koch.ambeth.merge;

/**
 * This extendable enables Ambeth to match the technical entities of an application to the internal entities of Ambeth. An example for this is the IAuditEntry.
 * Inside of the application the entity is defined to match the database, e.g. AuditEntry and this is then mapped to the IAuditEntry to tell ambeth how to find
 * meta data for this entity.
 */
public interface ITechnicalEntityTypeExtendable
{
	void registerTechnicalEntityType(Class<?> technicalEntityType, Class<?> entityType);

	void unregisterTechnicalEntityType(Class<?> technicalEntityType, Class<?> entityType);

	Class<?> getEntityTypeForTechnicalEntity(Class<?> technicalEntitiyType);
}
