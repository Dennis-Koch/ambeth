package de.osthus.ambeth.cache.imc;

import de.osthus.ambeth.merge.model.IObjRef;

public interface IInMemoryConfig
{
	IInMemoryConfig primitive(String memberName, Object value);

	IInMemoryConfig relation(String memberName, IObjRef... objRefs);

	IInMemoryConfig addRelation(String memberName, IObjRef... objRefs);

	IInMemoryConfig relation(String memberName, IInMemoryConfig... inMemoryConfigs);

	IInMemoryConfig addRelation(String memberName, IInMemoryConfig... inMemoryConfigs);
}