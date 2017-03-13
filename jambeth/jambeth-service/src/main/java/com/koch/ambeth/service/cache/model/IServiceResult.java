package com.koch.ambeth.service.cache.model;

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IServiceResult
{
	List<IObjRef> getObjRefs();

	Object getAdditionalInformation();
}
