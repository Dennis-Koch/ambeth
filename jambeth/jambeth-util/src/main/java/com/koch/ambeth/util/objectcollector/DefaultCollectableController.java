package com.koch.ambeth.util.objectcollector;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Constructor;

import com.koch.ambeth.util.ReflectUtil;

public class DefaultCollectableController implements ICollectableController {
	protected static final Object[] nullParams = new Object[0];

	protected final IObjectCollector objectCollector;
	protected final Constructor<?> constructor;
	protected final boolean isCollectable;
	protected final boolean isCollectorAware;

	public DefaultCollectableController(Class<?> type, IObjectCollector objectCollector)
			throws NoSuchMethodException {
		if (!ICollectable.class.isAssignableFrom(type)) {
			throw new IllegalStateException(
					"Class " + type.getName() + " neither does implement interface '"
							+ ICollectable.class.getName() + "' nor is an instance of '"
							+ ICollectableController.class.getName() + "' defined to handle this type");
		}
		Constructor<?>[] constructors = ReflectUtil.getConstructors(type);
		Constructor<?> constructorFound = null;
		for (Constructor<?> constructor : constructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length == 0 && constructorFound == null) {
				constructorFound = constructor;
			}
			else if (parameterTypes.length == 1 && IObjectCollector.class.equals(parameterTypes[0])) {
				constructorFound = constructor;
				break;
			}
		}
		this.objectCollector = objectCollector;
		constructor = constructorFound;
		isCollectable = ICollectable.class.isAssignableFrom(type);
		isCollectorAware = constructor.getParameterTypes().length == 1;
	}

	@Override
	public Object createInstance() throws Throwable {
		try {
			if (isCollectorAware) {
				return constructor.newInstance(objectCollector);
			}
			return constructor.newInstance(nullParams);
		}
		catch (Throwable e) {
			throw new RuntimeException(
					"Error occured while instantiating type " + constructor.getDeclaringClass(), e);
		}
	}

	@Override
	public void initObject(Object object) throws Throwable {
		if (isCollectable) {
			((ICollectable) object).initInternDoNotCall();
		}
	}

	@Override
	public void disposeObject(Object object) throws Throwable {
		if (isCollectable) {
			((ICollectable) object).disposeInternDoNotCall();
		}
	}
}
