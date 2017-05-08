package com.koch.ambeth.orm20.independent;

/*-
 * #%L
 * jambeth-test
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

public class EntityB {
	protected int idB;

	protected short versionB;

	protected String updatedByB, createdByB;

	protected long updatedOnB, createdOnB;

	public int getIdB() {
		return idB;
	}

	public void setIdB(int idB) {
		this.idB = idB;
	}

	public short getVersionB() {
		return versionB;
	}

	public void setVersionB(short versionB) {
		this.versionB = versionB;
	}

	public String getUpdatedByB() {
		return updatedByB;
	}

	public void setUpdatedByB(String updatedByB) {
		this.updatedByB = updatedByB;
	}

	public String getCreatedByB() {
		return createdByB;
	}

	public void setCreatedByB(String createdByB) {
		this.createdByB = createdByB;
	}

	public long getUpdatedOnB() {
		return updatedOnB;
	}

	public void setUpdatedOnB(long updatedOnB) {
		this.updatedOnB = updatedOnB;
	}

	public long getCreatedOnB() {
		return createdOnB;
	}

	public void setCreatedOnB(long createdOnB) {
		this.createdOnB = createdOnB;
	}
}
