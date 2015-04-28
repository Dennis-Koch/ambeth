package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.merge.model.IObjRef;

@XmlType
public interface ICacheRetriever extends IPropertyCacheRetriever
{
	List<ILoadContainer> getEntities(List<IObjRef> orisToLoad);
}
