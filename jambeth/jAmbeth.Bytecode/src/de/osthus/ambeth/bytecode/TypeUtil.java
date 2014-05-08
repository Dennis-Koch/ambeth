package de.osthus.ambeth.bytecode;

import java.lang.reflect.Modifier;

import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public final class TypeUtil
{
	public static final Type[] getClassesToTypes(Class<?>[] classes)
	{
		Type[] types = new Type[classes.length];
		for (int a = classes.length; a-- > 0;)
		{
			types[a] = Type.getType(classes[a]);
		}
		return types;
	}

	public static final int getModifiersToAccess(int modifiers)
	{
		int access = 0;
		if ((modifiers & Modifier.PUBLIC) != 0)
		{
			access |= Opcodes.ACC_PUBLIC;
		}
		if ((modifiers & Modifier.PRIVATE) != 0)
		{
			access |= Opcodes.ACC_PRIVATE;
		}
		if ((modifiers & Modifier.PROTECTED) != 0)
		{
			access |= Opcodes.ACC_PROTECTED;
		}
		if ((modifiers & Modifier.FINAL) != 0)
		{
			access |= Opcodes.ACC_FINAL;
		}
		if ((modifiers & Modifier.STATIC) != 0)
		{
			access |= Opcodes.ACC_STATIC;
		}
		if ((modifiers & Modifier.TRANSIENT) != 0)
		{
			access |= Opcodes.ACC_TRANSIENT;
		}
		return access;
	}

	public static boolean isPrimitive(Type type)
	{
		return Type.BOOLEAN_TYPE.equals(type) || Type.BYTE_TYPE.equals(type) || Type.CHAR_TYPE.equals(type) || Type.DOUBLE_TYPE.equals(type)
				|| Type.SHORT_TYPE.equals(type) || Type.FLOAT_TYPE.equals(type) || Type.INT_TYPE.equals(type) || Type.LONG_TYPE.equals(type);
	}

	private TypeUtil()
	{
		// Intended blank
	}
}
