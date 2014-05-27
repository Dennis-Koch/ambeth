package de.osthus.ambeth.orm;

import java.util.Set;

import org.w3c.dom.Document;

public interface IOrmXmlReader
{
	Set<EntityConfig> loadFromDocument(Document doc);

	void loadFromDocument(Document doc, Set<EntityConfig> localEntities, Set<EntityConfig> externalEntities);
}