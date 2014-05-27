package de.osthus.ambeth.cache.model;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.merge.model.IObjRef;

@XmlType
public interface IServiceResult
{
	List<IObjRef> getObjRefs();

	Object getAdditionalInformation();
}
