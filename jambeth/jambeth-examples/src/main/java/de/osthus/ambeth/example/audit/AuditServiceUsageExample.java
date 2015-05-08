package de.osthus.ambeth.example.audit;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.model.Audited;

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
