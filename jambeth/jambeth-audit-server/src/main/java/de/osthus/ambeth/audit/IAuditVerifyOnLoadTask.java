package de.osthus.ambeth.audit;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IAuditVerifyOnLoadTask
{
	void verifyEntitiesAsync(IList<IObjRef> objRefsToVerify);

	void verifyEntitiesSync(IList<IObjRef> objRefsToVerify);
}