package com.koch.ambeth.merge.orm.blueprint;

import org.w3c.dom.Document;

/**
 * Implement this interface to provide vom mapping for blueprint entities
 * 
 * @see IBlueprintPOrmrovider
 */
public interface IBlueprintVomProvider
{
	Document[] getVomDocuments();

	Document getVomDocument(String businessObjectType, String valueObjectType);
}
