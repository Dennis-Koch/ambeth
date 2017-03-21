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

/**
 * Entity based on a class with default and non default constructor
 */
public class EntityB extends EntityA
{
	private Long id;

	protected EntityB()
	{
		// Intended blank
	}

	@Override
	public Long getId()
	{
		return id;
	}

	public EntityB setId(Long id)
	{
		this.id = id;
		return this;
	}

	protected EntityB(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
}
