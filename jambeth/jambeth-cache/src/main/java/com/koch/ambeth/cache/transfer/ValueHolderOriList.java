package com.koch.ambeth.cache.transfer;

/*-
 * #%L
 * jambeth-cache
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

import java.util.ArrayList;
import java.util.Collection;

import com.koch.ambeth.service.merge.model.IObjRef;

// TODO [CollectionDataContract(IsReference = true, Namespace = "http://schema.kochdev.com/Ambeth")]
public class ValueHolderOriList extends ArrayList<IObjRef>
{

	private static final long serialVersionUID = 1L;

	public ValueHolderOriList()
	{
		super();
		// Intended blank
	}

	public ValueHolderOriList(Collection<IObjRef> collection)
	{
		super(collection);
		// Intended blank
	}

	public ValueHolderOriList(int capacity)
	{
		super(capacity);
		// Intended blank
	}

}
