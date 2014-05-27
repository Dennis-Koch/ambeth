package de.osthus.ambeth.ioc.extendable;

import net.sf.cglib.reflect.FastMethod;
import de.osthus.ambeth.util.IParamHolder;

public interface IExtendableRegistry
{
	FastMethod[] getAddRemoveMethods(Class<?> extendableInterface, Object[] arguments, IParamHolder<Object[]> linkArguments);

	FastMethod[] getAddRemoveMethods(Class<?> type, String eventName, Object[] arguments, IParamHolder<Object[]> linkArguments);

	FastMethod[] getAddRemoveMethods(Class<?> extendableInterface);

	FastMethod[] getAddRemoveMethods(Class<?> extendableInterface, Class<?>[] argumentTypes);
}