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

import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;

public class SelfReferencingEntity extends AbstractEntity
{
	protected String name;

	protected String[] values;

	protected Set<String> values2;

	protected SelfReferencingEntity relation1;

	protected SelfReferencingEntity relation2;

	protected SelfReferencingEntity()
	{
		// Intended blank
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String[] getValues()
	{
		return values;
	}

	public void setValues(String[] values)
	{
		this.values = values;
	}

	public Set<String> getValues2()
	{
		return values2;
	}

	public void setValues2(Set<String> values2)
	{
		this.values2 = values2;
	}

	public SelfReferencingEntity getRelation1()
	{
		return relation1;
	}

	public void setRelation1(SelfReferencingEntity relation1)
	{
		this.relation1 = relation1;
	}

	public SelfReferencingEntity getRelation2()
	{
		return relation2;
	}

	public void setRelation2(SelfReferencingEntity relation2)
	{
		this.relation2 = relation2;
	}
}
