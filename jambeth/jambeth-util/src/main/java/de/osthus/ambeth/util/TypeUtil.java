package de.osthus.ambeth.util;

import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public final class TypeUtil
{
	public static final Type[] getClassesToTypes(Class<?>[] classes)
	{
		Type[] types = new Type[classes.length];
		for (int a = classes.length; a-- > 0;)
		{
			Class<?> clazz = classes[a];
			if (clazz == null)
			{
				continue;
			}
			types[a] = Type.getType(clazz);
		}
		return types;
	}

	private TypeUtil()
	{
		// Intended blank
	}
}
