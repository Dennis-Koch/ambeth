package com.koch.ambeth.orm20.independent.a22b;

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

public class EntityA extends AbstractEntity {
	private EntityB b1;

	private EntityB b2;

	public EntityB getB1() {
		return b1;
	}

	public void setB1(EntityB b1) {
		this.b1 = b1;
	}

	public EntityB getB2() {
		return b2;
	}

	public void setB2(EntityB b2) {
		this.b2 = b2;
	}
}
