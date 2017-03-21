package com.koch.ambeth.server.helloworld.vo;

/*-
 * #%L
 * jambeth-server-helloworld
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

public class TestEntityVO extends AbstractEntityVO
{
	protected TestEntity2VO relation;

	protected List<TestEntity3VO> relations;

	protected int myValue;

	protected int myValueUnique;

	protected EmbeddedObjectVO embeddedObject;

	public EmbeddedObjectVO getEmbeddedObject()
	{
		return embeddedObject;
	}

	public void setEmbeddedObject(EmbeddedObjectVO embeddedObject)
	{
		this.embeddedObject = embeddedObject;
	}

	public void setMyValue(int myValue)
	{
		this.myValue = myValue;
	}

	public int getMyValue()
	{
		return myValue;
	}

	public void setMyValueUnique(int myValueUnique)
	{
		this.myValueUnique = myValueUnique;
	}

	public int getMyValueUnique()
	{
		return myValueUnique;
	}

	public TestEntity2VO getRelation()
	{
		return relation;
	}

	public void setRelation(TestEntity2VO relation)
	{
		this.relation = relation;
	}

	public List<TestEntity3VO> getRelations()
	{
		return relations;
	}

	public void setRelations(List<TestEntity3VO> relations)
	{
		this.relations = relations;
	}
}
