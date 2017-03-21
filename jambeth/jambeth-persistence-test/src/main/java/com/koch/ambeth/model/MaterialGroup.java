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

import com.koch.ambeth.util.annotation.EntityEqualsAspect;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
@EntityEqualsAspect
public class MaterialGroup {
	protected String id;

	protected short version;

	protected String name;

	protected String buid;

	protected MaterialGroup() {
		// Intended blank
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public short getVersion() {
		return version;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setBuid(String buid) {
		this.buid = buid;
	}

	public String getBuid() {
		return buid;
	}
}
