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

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;
import com.koch.ambeth.persistence.xml.model.TestEmbeddedType;

public abstract class OneToManyEntity extends AbstractEntity
{
	protected String buid;

	protected String name;

	protected List<String> nicknames;

	private Date needsSpecialMapping;

	protected TestEmbeddedType myEmbedded;

	protected List<OneToManyEntity> oneToManyEntities;

	protected List<OneToManyEntity> byListType;

	protected List<OneToManyEntity> byRefListType;

	protected Set<SelfReferencingEntity> selfReferencingEntities;

	protected OneToManyEntity()
	{
		// Intended blank
	}

	public String getBuid()
	{
		return buid;
	}

	public void setBuid(String buid)
	{
		this.buid = buid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getNicknames()
	{
		return nicknames;
	}

	public void setNicknames(List<String> nicknames)
	{
		this.nicknames = nicknames;
	}

	public Date getNeedsSpecialMapping()
	{
		return needsSpecialMapping;
	}

	public void setNeedsSpecialMapping(Date needsSpecialMapping)
	{
		this.needsSpecialMapping = needsSpecialMapping;
	}

	public TestEmbeddedType getMyEmbedded()
	{
		return myEmbedded;
	}

	public void setMyEmbedded(TestEmbeddedType myEmbedded)
	{
		this.myEmbedded = myEmbedded;
	}

	public List<OneToManyEntity> getOneToManyEntities()
	{
		return oneToManyEntities;
	}

	protected void setOneToManyEntities(List<OneToManyEntity> oneToManyEntities)
	{
		this.oneToManyEntities = oneToManyEntities;
	}

	public List<OneToManyEntity> getByListType()
	{
		return byListType;
	}

	protected void setByListType(List<OneToManyEntity> byListType)
	{
		this.byListType = byListType;
	}

	public List<OneToManyEntity> getByRefListType()
	{
		return byRefListType;
	}

	protected void setByRefListType(List<OneToManyEntity> byRefListType)
	{
		this.byRefListType = byRefListType;
	}

	public Set<SelfReferencingEntity> getSelfReferencingEntities()
	{
		return selfReferencingEntities;
	}

	protected void setSelfReferencingEntities(Set<SelfReferencingEntity> newSelfReferencingEntities)
	{
		selfReferencingEntities = newSelfReferencingEntities;
		// Set<SelfReferencingEntity> selfReferencingEntities = getSelfReferencingEntities();
		// if (selfReferencingEntities == null)
		// {
		// selfReferencingEntities = new HashSet<SelfReferencingEntity>();
		// this.selfReferencingEntities = selfReferencingEntities;
		// }
		// selfReferencingEntities.clear();
		// if (newSelfReferencingEntities != null)
		// {
		// selfReferencingEntities.addAll(newSelfReferencingEntities);
		// }
	}
}
