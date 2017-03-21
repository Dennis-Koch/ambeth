package com.koch.ambeth.bytecode;

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

import com.koch.ambeth.merge.IEntityFactory;

/**
 * Entity based on a class with non default constructor. If an entity does not have a default
 * constructor EntityManager expects a constructor having {@link IEntityFactory} as parameter. Other
 * constructors are not used by EntityManager.
 */
public class EntityC extends AbstractEntity {
	protected Long id;

	protected String name;

	protected String value;

	protected EntityC(IEntityFactory entityFactory) {
		super(entityFactory);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	/**
	 * "Normal" api
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	/**
	 * "Fluent" api
	 */
	public EntityC setValue(String value) {
		this.value = value;
		return this;
	}
}
