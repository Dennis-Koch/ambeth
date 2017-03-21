package com.koch.ambeth.log.exceptions;

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

public class PropertyResolveException extends RuntimeException {
	private static final long serialVersionUID = -7149333583589679782L;

	public PropertyResolveException() {
		super();
	}

	protected PropertyResolveException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PropertyResolveException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertyResolveException(String message) {
		super(message);
	}

	public PropertyResolveException(Throwable cause) {
		super(cause);
	}

}
