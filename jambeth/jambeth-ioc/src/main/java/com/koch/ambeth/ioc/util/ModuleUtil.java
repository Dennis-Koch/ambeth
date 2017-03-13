package com.koch.ambeth.ioc.util;

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
