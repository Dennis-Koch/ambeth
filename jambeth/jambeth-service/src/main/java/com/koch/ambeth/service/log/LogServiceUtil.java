package com.koch.ambeth.service.log;

import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.NoOpStateRollback;

public final class LogServiceUtil {
	private static final ThreadLocal<Boolean> suppressedExplicitExceptionLoggingTL =
			new ThreadLocal<>();

	public static IStateRollback pushSuppressExplicitExceptionLogging(IStateRollback... rollbacks) {
		final Boolean oldValue = suppressedExplicitExceptionLoggingTL.get();
		if (Boolean.TRUE.equals(oldValue)) {
			return NoOpStateRollback.createNoOpRollback(rollbacks);
		}
		suppressedExplicitExceptionLoggingTL.set(Boolean.TRUE);
		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Exception {
				suppressedExplicitExceptionLoggingTL.set(oldValue);
			}
		};
	}

	public static boolean isExplicitExceptionLoggingEnabled() {
		return !Boolean.TRUE.equals(suppressedExplicitExceptionLoggingTL.get());
	}

	private LogServiceUtil() {
		// intended blank
	}
}
