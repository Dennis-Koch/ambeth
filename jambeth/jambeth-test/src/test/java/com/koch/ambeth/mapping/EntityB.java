package com.koch.ambeth.mapping;

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

public class EntityB
{
	protected int id;

	protected String nameOfB;

	public String getNameOfB()
	{
		return nameOfB;
	}

	public void setNameOfB(String nameOfB)
	{
		this.nameOfB = nameOfB;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public EntityA getEntityA()
	{
		return entityA;
	}

	public void setEntityA(EntityA entityA)
	{
		this.entityA = entityA;
	}

	protected EntityA entityA;

}
