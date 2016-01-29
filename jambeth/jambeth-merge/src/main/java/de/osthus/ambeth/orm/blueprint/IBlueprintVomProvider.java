package de.osthus.ambeth.orm.blueprint;

import org.w3c.dom.Document;

/**
 * Implement this interface to provide vom mapping for blueprint entities
 * 
 * @see IBlueprintPOrmrovider
 */
public interface IBlueprintVomProvider
{
	Document[] getVomDocuments();

	Document getVomDocument(IEntityTypeBlueprint entityTypeBlueprint);
}
