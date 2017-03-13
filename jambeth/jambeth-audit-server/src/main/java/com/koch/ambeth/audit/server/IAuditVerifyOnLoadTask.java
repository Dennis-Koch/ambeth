package com.koch.ambeth.audit.server;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public interface IAuditVerifyOnLoadTask
{
	void verifyEntitiesAsync(IList<IObjRef> objRefsToVerify);

	void verifyEntitiesSync(IList<IObjRef> objRefsToVerify);
}