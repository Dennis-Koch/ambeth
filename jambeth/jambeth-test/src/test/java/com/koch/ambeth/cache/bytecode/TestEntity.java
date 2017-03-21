package com.koch.ambeth.cache.bytecode;

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

import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.model.AbstractEntity;

public abstract class TestEntity extends AbstractEntity {
	protected List<TestEntity> f_children;

	protected List<TestEntity> childrenWithProtectedField;

	protected List<TestEntity> childrenWithPrivateField;

	protected TestEntity(IEntityFactory entityFactory) {
		super();
	}

	public List<TestEntity> getChildrenNoField() {
		return f_children;
	}

	public void setChildrenNoField(List<TestEntity> children) {
		f_children = children;
	}

	public List<TestEntity> getChildrenWithProtectedField() {
		return childrenWithProtectedField;
	}

	public void setChildrenWithProtectedField(List<TestEntity> childrenWithProtectedField) {
		this.childrenWithProtectedField = childrenWithProtectedField;
	}

	public List<TestEntity> getChildrenWithPrivateField() {
		return childrenWithPrivateField;
	}

	public void setChildrenWithPrivateField(List<TestEntity> childrenWithPrivateField) {
		this.childrenWithPrivateField = childrenWithPrivateField;
	}
}
