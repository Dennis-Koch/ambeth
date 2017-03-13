package com.koch.ambeth.bytecode.behavior;

import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.bytecode.IEnhancementHint;

public interface IBytecodeBehaviorState
{
	Class<?> getOriginalType();

	Class<?> getCurrentType();

	Type getNewType();

	IServiceContext getBeanContext();

	IEnhancementHint getContext();

	<T extends IEnhancementHint> T getContext(Class<T> contextType);

	PropertyInstance getProperty(String propertyName, Class<?> propertyType);

	PropertyInstance getProperty(String propertyName, Type propertyType);

	MethodInstance[] getAlreadyImplementedMethodsOnNewType();

	FieldInstance getAlreadyImplementedField(String fieldName);

	boolean hasMethod(MethodInstance method);

	boolean isMethodAlreadyImplementedOnNewType(MethodInstance method);
}
