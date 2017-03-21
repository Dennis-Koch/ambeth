package com.koch.ambeth.merge.model;

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

import com.koch.ambeth.service.merge.model.AbstractMethodLifecycleExtension;
import com.koch.ambeth.service.merge.model.IEntityMetaData;

public class PrePersistMethodLifecycleExtension extends AbstractMethodLifecycleExtension
{
	@Override
	public void postCreate(IEntityMetaData metaData, Object newEntity)
	{
		// intended blank
	}

	@Override
	public void postLoad(IEntityMetaData metaData, Object entity)
	{
		// intended blank
	}

	@Override
	public void prePersist(IEntityMetaData metaData, Object entity)
	{
		callMethod(entity, "PrePersist");
	}
}
