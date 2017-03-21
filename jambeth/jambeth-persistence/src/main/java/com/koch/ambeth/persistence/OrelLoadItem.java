package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.merge.model.IObjRef;

public class OrelLoadItem
{
	protected final IObjRef objRef;

	protected final IObjRelation objRel;

	public OrelLoadItem(IObjRef objRef, IObjRelation objRel)
	{
		this.objRef = objRef;
		this.objRel = objRel;
	}

	public IObjRef getObjRef()
	{
		return objRef;
	}

	public IObjRelation getObjRel()
	{
		return objRel;
	}
}
