package com.koch.ambeth.relations.many.lazy.link.reverse.none;

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

import com.koch.ambeth.model.AbstractEntity;

public class EntityB extends AbstractEntity {
	protected String name;

	protected List<EntityA> entityAs;

	protected EntityB() {
		// Intended blank
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<EntityA> getEntityAs() {
		return entityAs;
	}

	public void setEntityAs(List<EntityA> entityAs) {
		this.entityAs = entityAs;
	}
}
