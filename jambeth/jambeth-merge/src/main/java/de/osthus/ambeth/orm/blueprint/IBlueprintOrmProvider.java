package de.osthus.ambeth.orm.blueprint;

import org.w3c.dom.Document;

/**
 * Implement this interface to provide orm mapping for blueprint entities
 * 
 * @see IBlueprintProvider
 */
public interface IBlueprintOrmProvider
{
	Document[] getOrmDocuments();

	Document getOrmDocument(IEntityTypeBlueprint entityTypeBlueprint);
}
