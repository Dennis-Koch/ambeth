package com.koch.ambeth.ioc.util;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.util.collections.LinkedHashSet;

public final class ModuleUtil
{
	private ModuleUtil()
	{
	}

	public static Class<?>[] mergeModules(Class<?>[] leftModules, Class<?>... rightModules)
	{
		if (leftModules == null)
		{
			return rightModules;
		}
		else if (rightModules == null)
		{
			return leftModules;
		}
		LinkedHashSet<Class<?>> modules = new LinkedHashSet<Class<?>>(leftModules.length + rightModules.length);
		for (int a = 0, size = leftModules.length; a < size; a++)
		{
			modules.add(leftModules[a]);
		}
		for (int a = 0, size = rightModules.length; a < size; a++)
		{
			modules.add(rightModules[a]);
		}
		return modules.toArray(new Class<?>[modules.size()]);
	}
}
