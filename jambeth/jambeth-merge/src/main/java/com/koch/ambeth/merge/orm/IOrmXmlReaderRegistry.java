package com.koch.ambeth.merge.orm;

public interface IOrmXmlReaderRegistry
{
	IOrmXmlReader getOrmXmlReader(String version);
}
