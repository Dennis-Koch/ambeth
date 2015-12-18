package de.osthus.ambeth.orm.blueprint;

/**
 * Implement this interface to load blueprint entities from db/file/memory
 * 
 * @see IBlueprintOrmProvider
 */
public interface IBlueprintProvider
{
	IEntityTypeBlueprint resolveEntityTypeBlueprint(String entityTypeName);
}
