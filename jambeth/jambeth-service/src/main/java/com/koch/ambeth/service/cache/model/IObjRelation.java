package com.koch.ambeth.service.cache.model;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IObjRelation extends IPrintable
{
	String getMemberName();

	IObjRef[] getObjRefs();

	Class<?> getRealType();

	Object getVersion();
}
