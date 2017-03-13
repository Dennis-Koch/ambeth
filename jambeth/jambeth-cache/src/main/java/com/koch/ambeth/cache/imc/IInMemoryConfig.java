package com.koch.ambeth.cache.imc;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface IInMemoryConfig
{
	IInMemoryConfig primitive(String memberName, Object value);

	IInMemoryConfig relation(String memberName, IObjRef... objRefs);

	IInMemoryConfig addRelation(String memberName, IObjRef... objRefs);

	IInMemoryConfig relation(String memberName, IInMemoryConfig... inMemoryConfigs);

	IInMemoryConfig addRelation(String memberName, IInMemoryConfig... inMemoryConfigs);
}