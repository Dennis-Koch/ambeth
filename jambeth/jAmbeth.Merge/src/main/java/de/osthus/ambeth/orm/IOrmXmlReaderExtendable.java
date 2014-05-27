package de.osthus.ambeth.orm;

public interface IOrmXmlReaderExtendable
{
	void registerOrmXmlReader(IOrmXmlReader reader, String version);

	void unregisterOrmXmlReader(IOrmXmlReader reader, String version);
}
