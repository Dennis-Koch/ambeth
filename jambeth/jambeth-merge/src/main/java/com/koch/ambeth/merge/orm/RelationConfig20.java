package com.koch.ambeth.merge.orm;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.util.ParamChecker;

public class RelationConfig20 implements IRelationConfig
{
	private final String name;

	private final ILinkConfig link;

	private EntityIdentifier entityIdentifier;

	private boolean explicitlyNotMergeRelevant;

	public RelationConfig20(String name, ILinkConfig link)
	{
		ParamChecker.assertParamNotNullOrEmpty(name, "name");
		ParamChecker.assertParamNotNull(link, "link");

		this.name = name;
		this.link = link;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public ILinkConfig getLink()
	{
		return link;
	}

	public EntityIdentifier getEntityIdentifier()
	{
		return entityIdentifier;
	}

	public void setEntityIdentifier(EntityIdentifier entityIdentifier)
	{
		this.entityIdentifier = entityIdentifier;
	}

	@Override
	public boolean isExplicitlyNotMergeRelevant()
	{
		return explicitlyNotMergeRelevant;
	}

	public void setExplicitlyNotMergeRelevant(boolean explicitlyNotMergeRelevant)
	{
		this.explicitlyNotMergeRelevant = explicitlyNotMergeRelevant;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof RelationConfig20)
		{
			IRelationConfig other = (IRelationConfig) obj;
			return name.equals(other.getName());
		}
		else
		{
			return false;
		}
	}
}
