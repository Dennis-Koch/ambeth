package com.koch.ambeth.transfer;

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

public class MaterialSmallVO {
	protected int id;

	protected short version;

	protected String buid;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public short getVersion() {
		return version;
	}

	public void setBuid(String buid) {
		this.buid = buid;
	}

	public String getBuid() {
		return buid;
	}

	@Override
	public int hashCode() {
		return id ^ MaterialSmallVO.class.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MaterialSmallVO)) {
			return false;
		}
		MaterialSmallVO other = (MaterialSmallVO) obj;
		return other.id == id && other.version == version;
	}
}
