package com.koch.ambeth.example.audit;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.audit.model.Audited;

@Audited
public class AuditServiceUsageExample implements IMyAuditedService {
	@LogInstance
	private ILogger log;

	@Override
	public boolean myAuditedMethod(String someArg) {
		if (log.isInfoEnabled()) {
			log.info("This call will be audited: " + someArg);
		}
		return true;
	}

	@Audited(false)
	@Override
	public boolean myNotAuditedMethod(String someArg) {
		if (log.isInfoEnabled()) {
			log.info("This call is not audited: " + someArg);
		}
		return false;
	}
}
