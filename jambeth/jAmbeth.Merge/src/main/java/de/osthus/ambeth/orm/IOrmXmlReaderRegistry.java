package de.osthus.ambeth.orm;

public interface IOrmXmlReaderRegistry
{
	IOrmXmlReader getOrmXmlReader(String version);
}
