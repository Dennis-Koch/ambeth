package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-information-bus-test
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.koch.ambeth.log.LogLevel;
import com.koch.ambeth.log.AmbethLogger;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ListLogger extends AmbethLogger {
	protected static final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

	protected final IList<String> debugEntries = new ArrayList<>(),
			infoEntries = new ArrayList<>(), warnEntries = new ArrayList<>(),
			errorEntries = new ArrayList<>();

	public ListLogger(String source) {
		super(source);
	}

	@Override
	public DateFormat getFormat() {
		return format;
	}

	public IList<String> getDebugEntries() {
		return debugEntries;
	}

	public IList<String> getInfoEntries() {
		return infoEntries;
	}

	public IList<String> getWarnEntries() {
		return warnEntries;
	}

	public IList<String> getErrorEntries() {
		return errorEntries;
	}

	protected void log(LogLevel logLevel, boolean errorLog, String output) {
		switch (logLevel) {
			case INFO: {
				infoEntries.add(output);
				break;
			}
			case DEBUG: {
				debugEntries.add(output);
				break;
			}
			case WARN: {
				warnEntries.add(output);
				break;
			}
			case ERROR: {
				errorEntries.add(output);
				break;
			}
			default:
				RuntimeExceptionUtil.createEnumNotSupportedException(logLevel);
		}
	}
}
