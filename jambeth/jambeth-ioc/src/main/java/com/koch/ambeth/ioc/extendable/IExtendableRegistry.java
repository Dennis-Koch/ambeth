package com.koch.ambeth.ioc.extendable;

import com.koch.ambeth.util.IParamHolder;

import net.sf.cglib.reflect.FastMethod;

public interface IExtendableRegistry
{
	FastMethod[] getAddRemoveMethods(Class<?> extendableInterface, Object[] arguments, IParamHolder<Object[]> linkArguments);

	FastMethod[] getAddRemoveMethods(Class<?> type, String eventName, Object[] arguments, IParamHolder<Object[]> linkArguments);

	FastMethod[] getAddRemoveMethods(Class<?> extendableInterface);

	FastMethod[] getAddRemoveMethods(Class<?> extendableInterface, Class<?>[] argumentTypes);
}