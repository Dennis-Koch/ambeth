package com.koch.ambeth.cache.service;

import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface ICacheService extends ICacheRetriever
{
	IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription);
}
