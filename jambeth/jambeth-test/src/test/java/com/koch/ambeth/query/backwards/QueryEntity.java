package com.koch.ambeth.query.backwards;

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

import com.koch.ambeth.model.AbstractEntity;

public class QueryEntity extends AbstractEntity {
	protected String name;

	protected QueryEntity next;

	protected LinkTableEntity linkTableEntity;

	protected QueryEntity() {
		// Intended blank
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public QueryEntity getNext() {
		return next;
	}

	public void setNext(QueryEntity next) {
		this.next = next;
	}

	public LinkTableEntity getLinkTableEntity() {
		return linkTableEntity;
	}

	public void setLinkTableEntity(LinkTableEntity linkTableEntity) {
		this.linkTableEntity = linkTableEntity;
	}
}
