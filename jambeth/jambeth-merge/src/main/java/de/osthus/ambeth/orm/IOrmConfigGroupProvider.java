package de.osthus.ambeth.orm;

import org.w3c.dom.Document;

public interface IOrmConfigGroupProvider
{
	IOrmConfigGroup getOrmConfigGroup(String xmlFileNames);

	IOrmConfigGroup getOrmConfigGroup(Document[] docs, IOrmEntityTypeProvider ormEntityTypeProvider);
}