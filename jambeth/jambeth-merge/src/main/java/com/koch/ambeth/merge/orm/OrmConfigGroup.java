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

import com.koch.ambeth.util.collections.ISet;

public class OrmConfigGroup implements IOrmConfigGroup
{
	protected final ISet<IEntityConfig> localEntityConfigs;

	protected final ISet<IEntityConfig> externalEntityConfigs;

	public OrmConfigGroup(ISet<IEntityConfig> localEntityConfigs, ISet<IEntityConfig> externalEntityConfigs)
	{
		this.localEntityConfigs = localEntityConfigs;
		this.externalEntityConfigs = externalEntityConfigs;
	}

	@Override
	public Iterable<IEntityConfig> getExternalEntityConfigs()
	{
		return externalEntityConfigs;
	}

	@Override
	public Iterable<IEntityConfig> getLocalEntityConfigs()
	{
		return localEntityConfigs;
	}
}
