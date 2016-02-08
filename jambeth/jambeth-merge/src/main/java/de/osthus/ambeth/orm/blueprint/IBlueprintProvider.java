package de.osthus.ambeth.orm.blueprint;

import java.util.List;

/**
 * Implement this interface to load blueprint entities from db/file/memory
 * 
 * @see IBlueprintOrmProvider
 */
public interface IBlueprintProvider
{
	IEntityTypeBlueprint resolveEntityTypeBlueprint(String entityTypeName);

	Class<?> getDefaultInterface();

	List<? extends IEntityTypeBlueprint> getAll();
}
