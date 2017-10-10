package com.koch.ambeth.service.merge.model;

import com.koch.ambeth.util.collections.IList;

public interface IObjRefType {
	IObjRef getObjRef();

	IObjRef getObjRef(String identifierMemberName);

	IList<IObjRef> getAllObjRefs();
}
