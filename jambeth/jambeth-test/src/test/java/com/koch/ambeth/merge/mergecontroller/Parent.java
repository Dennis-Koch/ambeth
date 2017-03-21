package com.koch.ambeth.merge.mergecontroller;

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
import java.util.Set;

import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.model.AbstractEntity;

public abstract class Parent extends AbstractEntity
{
	protected Child child;

	protected String name;

	protected IEntityFactory abc;

	protected Parent(IEntityFactory entityFactory)
	{
		abc = entityFactory;
		// Intended blank
	}

	public Child getChild()
	{
		return child;
	}

	public void setChild(Child child)
	{
		this.child = child;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public abstract List<Child> getOtherChildren();

	public abstract Parent setOtherChildren(List<Child> children);

	public abstract Set<Child> getOtherChildren2();

	public abstract Parent setOtherChildren2(Set<Child> children2);
}
