package de.osthus.ambeth.service;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.model.IServiceDescription;

@XmlType
public interface ICacheService extends ICacheRetriever
{
	IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription);
}
