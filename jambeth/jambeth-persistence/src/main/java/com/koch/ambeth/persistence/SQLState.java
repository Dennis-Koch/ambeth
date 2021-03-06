package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

public enum SQLState {
	CONNECTION_NOT_OPEN("08003"), //
	NULL_CONSTRAINT("23502"), //
	UNIQUE_CONSTRAINT("23505"), //
	ACCESS_VIOLATION("42000"), //
	LOCK_NOT_AVAILABLE("55P03");

	private String xopen;

	private SQLState(String bothCode) {
		xopen = bothCode;
	}

	public String getXopen() {
		return xopen;
	}

	public String getMessage() {
		return name();
	}
}
