package com.koch.ambeth.ioc.extendable;

import java.lang.reflect.Method;

import com.koch.ambeth.util.IParamHolder;

public interface IExtendableRegistry {
	Method[] getAddRemoveMethods(Class<?> extendableInterface, Object[] arguments,
			IParamHolder<Object[]> linkArguments);

	Method[] getAddRemoveMethods(Class<?> type, String eventName, Object[] arguments,
			IParamHolder<Object[]> linkArguments);

	Method[] getAddRemoveMethods(Class<?> extendableInterface);

	Method[] getAddRemoveMethods(Class<?> extendableInterface, Class<?>[] argumentTypes);
}
