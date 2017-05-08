package com.koch.ambeth.model;

/*-
 * #%L
 * jambeth-persistence-test
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

public class Unit {
	protected Object id;

	protected String name;

	protected long version;

	protected String buid;

	protected Unit() {
		// Intended blank
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public long getVersion() {
		return version;
	}

	public void setBuid(String buid) {
		this.buid = buid;
	}

	public String getBuid() {
		return buid;
	}
}
