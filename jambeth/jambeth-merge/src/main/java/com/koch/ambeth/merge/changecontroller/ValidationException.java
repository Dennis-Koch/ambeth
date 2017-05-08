package com.koch.ambeth.merge.changecontroller;

/*-
 * #%L
 * jambeth-merge
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
 * A validation exception is thrown if a validation (application logic) fails.
 */
public class ValidationException extends RuntimeException {
	private static final long serialVersionUID = -4537210552824483170L;

	private final Object affectedEntity;

	public ValidationException(String message) {
		this(message, null);
	}

	public ValidationException(String message, Object entity) {
		super(message);
		affectedEntity = entity;
	}

	public Object getAffectedEntity() {
		return affectedEntity;
	}
}
