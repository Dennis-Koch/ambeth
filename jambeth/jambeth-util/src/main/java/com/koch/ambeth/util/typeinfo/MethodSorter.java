package com.koch.ambeth.util.typeinfo;

import java.lang.reflect.Method;

public final class MethodSorter
{
	public static void sort(final Method[] methods)
	{
		while (true)
		{
			boolean changed = false;
			for (int a = 0; a < methods.length - 1; a++)
			{
				Method oneMethod = methods[a], otherMethod = methods[a + 1];
				int comp = oneMethod.toString().compareTo(otherMethod.toString());
				if (comp > 0)
				{
					methods[a] = otherMethod;
					methods[a + 1] = oneMethod;
					changed = true;
				}
			}
			if (!changed)
			{
				break;
			}
		}
	}

	private MethodSorter()
	{
		// intended blank
	}
}
