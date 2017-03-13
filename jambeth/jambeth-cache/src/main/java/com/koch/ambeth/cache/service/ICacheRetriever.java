package com.koch.ambeth.cache.service;

import java.util.List;

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface ICacheRetriever extends IRelationRetriever
{
	List<ILoadContainer> getEntities(List<IObjRef> orisToLoad);
}
