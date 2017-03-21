package com.koch.ambeth.persistence.jdbc.mapping.models;

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

public class SelfReferencingEntityVO extends AbstractEntity
{
	protected String name;

	protected List<String> values;

	protected StringListType values2List;

	protected String relation1;

	protected String relation2;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getValues()
	{
		return values;
	}

	public void setValues(List<String> values)
	{
		this.values = values;
	}

	public StringListType getValues2List()
	{
		return values2List;
	}

	public void setValues2List(StringListType values2List)
	{
		this.values2List = values2List;
	}

	public String getRelation1()
	{
		return relation1;
	}

	public void setRelation1(String relation1)
	{
		this.relation1 = relation1;
	}

	public String getRelation2()
	{
		return relation2;
	}

	public void setRelation2(String relation2)
	{
		this.relation2 = relation2;
	}

}
