package com.koch.ambeth.cache.util;

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

import com.koch.ambeth.merge.util.DirectValueHolderRef;

public class PrefetchCommand
{
	public final DirectValueHolderRef valueHolder;

	public final PrefetchPath[] prefetchPaths;

	public PrefetchCommand(DirectValueHolderRef valueHolder, PrefetchPath[] cachePaths)
	{
		this.valueHolder = valueHolder;
		this.prefetchPaths = cachePaths;
	}

	@Override
	public int hashCode()
	{
		return valueHolder.hashCode() ^ prefetchPaths.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof PrefetchCommand))
		{
			return false;
		}
		PrefetchCommand other = (PrefetchCommand) obj;
		// Use equals() of ValueHolderKey
		return valueHolder.equals(other.valueHolder) && prefetchPaths == other.prefetchPaths;
	}
}
