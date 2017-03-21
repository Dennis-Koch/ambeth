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

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ObjectCollectorItem implements IObjectCollectorItem {
	protected final ArrayList<ICollectable> unusedList = new ArrayList<ICollectable>();

	protected int lowestSizeSinceLastCheck = 0;

	protected final Class<? extends ICollectable> constructorClass;

	protected final IObjectCollector objectCollector;

	protected final boolean collectorAware;

	public ObjectCollectorItem(IObjectCollector objectCollector,
			Class<? extends ICollectable> constructorClass) {
		this.objectCollector = objectCollector;
		this.constructorClass = constructorClass;
		collectorAware = IObjectCollectorAware.class.isAssignableFrom(constructorClass);
	}

	@Override
	public Object getOneInstance() {
		ICollectable elem = popLastElement();
		int listSize = unusedList.size();
		if (elem != null) {
			if (lowestSizeSinceLastCheck > listSize) {
				lowestSizeSinceLastCheck = listSize;
			}
		}
		else {
			try {
				elem = constructorClass.newInstance();
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		if (collectorAware) {
			((IObjectCollectorAware) elem).setObjectCollector(objectCollector);
		}
		elem.initInternDoNotCall();
		return elem;

	}

	protected ICollectable popLastElement() {
		return unusedList.popLastElement();
	}

	@Override
	public void dispose(final Object object) {
		ICollectable obj = (ICollectable) object;
		obj.disposeInternDoNotCall();
		if (collectorAware) {
			((IObjectCollectorAware) object).setObjectCollector(null);
		}
		unusedList.add(obj);
	}

	@Override
	public void cleanUp() {
		while (lowestSizeSinceLastCheck-- > 0) {
			unusedList.popLastElement();
		}
		lowestSizeSinceLastCheck = unusedList.size();
	}
}
