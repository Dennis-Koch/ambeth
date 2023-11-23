package com.koch.ambeth.service.log;

import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public final class LogServiceUtil {
    private static final ThreadLocal<Boolean> suppressedExplicitExceptionLoggingTL = new SensitiveThreadLocal<>();

    public static IStateRollback pushSuppressExplicitExceptionLogging() {
        var oldValue = suppressedExplicitExceptionLoggingTL.get();
        if (Boolean.TRUE.equals(oldValue)) {
            return StateRollback.empty();
        }
        suppressedExplicitExceptionLoggingTL.set(Boolean.TRUE);
        return () -> suppressedExplicitExceptionLoggingTL.set(oldValue);
    }

    public static boolean isExplicitExceptionLoggingEnabled() {
        return !Boolean.TRUE.equals(suppressedExplicitExceptionLoggingTL.get());
    }

    private LogServiceUtil() {
        // intended blank
    }
}
