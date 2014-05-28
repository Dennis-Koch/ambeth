package de.osthus.ambeth.bytecode.util;

import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.cache.ValueHolderIEC;
import de.osthus.ambeth.util.ReflectUtil;

public class EnhancerUtil
{
	public static final MethodInstance getSuperSetter(PropertyInstance propertyInfo)
	{
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		Class<?> superType = state.getCurrentType();
		java.lang.reflect.Method superSetter = ReflectUtil.getDeclaredMethod(false, superType, propertyInfo.getSetter().getName(), propertyInfo.getSetter()
				.getParameters());
		return new MethodInstance(superSetter);
	}

	public static final MethodInstance getSuperGetter(PropertyInstance propertyInfo)
	{
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		Class<?> superType = state.getCurrentType();
		java.lang.reflect.Method superGetter = ReflectUtil.getDeclaredMethod(true, superType,
				getGetterNameOfRelationPropertyWithNoInit(propertyInfo.getName()), propertyInfo.getGetter().getParameters());
		if (superGetter == null)
		{
			// not a relation -> no lazy loading
			superGetter = ReflectUtil.getDeclaredMethod(false, superType, propertyInfo.getGetter().getName(), propertyInfo.getGetter().getParameters());
		}
		return new MethodInstance(superGetter);
	}

	public static final String getGetterNameOfRelationPropertyWithNoInit(String propertyName)
	{
		return "get" + propertyName + ValueHolderIEC.getNoInitSuffix();
	}
}
