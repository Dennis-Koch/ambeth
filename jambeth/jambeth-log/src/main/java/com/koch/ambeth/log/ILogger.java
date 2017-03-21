package com.koch.ambeth.log;

/*-
 * #%L
 * jambeth-log
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

public interface ILogger
{
	boolean isDebugEnabled();

	boolean isInfoEnabled();

	boolean isWarnEnabled();

	boolean isErrorEnabled();

	void debug(CharSequence message);

	void debug(CharSequence message, Throwable e);

	void debug(Throwable e);

	void info(CharSequence message);

	void info(CharSequence message, Throwable e);

	void info(Throwable e);

	void warn(CharSequence message);

	void warn(CharSequence message, Throwable e);

	void warn(Throwable e);

	void error(CharSequence message);

	void error(CharSequence message, Throwable e);

	void error(Throwable e);

}
