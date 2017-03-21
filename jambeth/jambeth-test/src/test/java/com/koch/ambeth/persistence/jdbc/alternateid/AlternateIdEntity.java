package com.koch.ambeth.persistence.jdbc.alternateid;

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

import java.util.List;

import javax.persistence.PrePersist;

import com.koch.ambeth.model.AbstractEntity;

public class AlternateIdEntity extends AbstractEntity {
	protected String name;

	protected BaseEntity baseEntity;

	protected List<BaseEntity2> baseEntities2;

	protected AlternateIdEntity() {
		// Intended blank
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BaseEntity getBaseEntity() {
		return baseEntity;
	}

	public void setBaseEntity(BaseEntity baseEntity) {
		this.baseEntity = baseEntity;
	}

	public List<BaseEntity2> getBaseEntities2() {
		return baseEntities2;
	}

	public void setBaseEntities2(List<BaseEntity2> baseEntities2) {
		this.baseEntities2 = baseEntities2;
	}

	@PrePersist
	public void prePersist() {
		if (getName() == null || getName().isEmpty()) {
			setName(Long.toString(System.currentTimeMillis()));
		}
	}
}
