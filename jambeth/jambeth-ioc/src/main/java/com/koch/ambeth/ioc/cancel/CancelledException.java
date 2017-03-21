package com.koch.ambeth.ioc.cancel;

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

/**
 * Thrown by {@link ICancellation#ensureNotCancelled()} if {@link ICancellation#isCancelled()} returns true
 */
public class CancelledException extends RuntimeException
{
	private static final long serialVersionUID = -3110298100220185217L;

	public CancelledException()
	{
	}

	public CancelledException(String message)
	{
		super(message);
	}

	public CancelledException(Throwable cause)
	{
		super(cause);
	}

	public CancelledException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CancelledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
