package com.koch.ambeth.xml.oriwrapper;

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
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public class EntityA extends AbstractEntity {
	protected List<EntityB> entityBs;

	protected EntityA() {
		// Intended blank
	}

	public List<EntityB> getEntityBs() {
		return entityBs;
	}

	public void setEntityBs(List<EntityB> entityBs) {
		this.entityBs = entityBs;
	}
}
