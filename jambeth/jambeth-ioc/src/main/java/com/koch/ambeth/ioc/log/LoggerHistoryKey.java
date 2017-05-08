package com.koch.ambeth.ioc.log;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.koch.ambeth.log.ILogger;

public class LoggerHistoryKey extends WeakReference<ILogger> {
	public final Reference<Object> contextHandleR;

	public final String logTextForHistory;

	public final int hash;

	public LoggerHistoryKey(ILogger logger, Reference<Object> contextHandleR,
			String logTextForHistory) {
		super(logger);
		this.contextHandleR = contextHandleR;
		this.logTextForHistory = logTextForHistory;
		int hash = 11;
		if (logger != null) {
			hash ^= logger.hashCode();
		}
		Object contextHandle = contextHandleR.get();
		if (contextHandle != null) {
			hash ^= contextHandle.hashCode();
		}
		this.hash = hash ^ logTextForHistory.hashCode();
	}

	public boolean isValid() {
		return get() != null && contextHandleR.get() != null;
	}

	@Override
	public int hashCode() {
		// Hash MUST be precalculated because of hash requirement for removal after the Refs are null
		// (and therefore the hash would have changed)
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LoggerHistoryKey)) {
			return false;
		}
		Object contextHandle = contextHandleR.get();
		if (contextHandle == null) {
			// Cleanup contextHandle is never equal with anything
			return false;
		}
		ILogger logger = get();
		if (logger == null) {
			// Cleanup logger is never equal with anything
			return false;
		}
		LoggerHistoryKey other = (LoggerHistoryKey) obj;
		Object contextHandle2 = other.contextHandleR.get();
		if (contextHandle2 == null) {
			// Cleanup contextHandle is never equal with anything
			return false;
		}
		ILogger logger2 = other.get();
		if (logger2 == null) {
			// Cleanup logger is never equal with anything
			return false;
		}
		return logTextForHistory.equals(other.logTextForHistory) && contextHandle.equals(contextHandle2)
				&& logger.equals(logger2);
	}
}
