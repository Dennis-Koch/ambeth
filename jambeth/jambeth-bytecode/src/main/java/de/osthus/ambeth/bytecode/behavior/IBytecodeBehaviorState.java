package de.osthus.ambeth.bytecode.behavior;

import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public interface IBytecodeBehaviorState
{
	Class<?> getOriginalType();

	Class<?> getCurrentType();

	Type getNewType();

	IServiceContext getBeanContext();

	IEnhancementHint getContext();

	<T extends IEnhancementHint> T getContext(Class<T> contextType);

	PropertyInstance getProperty(String propertyName);

	MethodInstance[] getAlreadyImplementedMethodsOnNewType();

	FieldInstance getAlreadyImplementedField(String fieldName);

	boolean hasMethod(MethodInstance method);

	boolean isMethodAlreadyImplementedOnNewType(MethodInstance method);
}
