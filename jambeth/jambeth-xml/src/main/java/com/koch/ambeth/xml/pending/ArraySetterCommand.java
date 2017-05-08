package com.koch.ambeth.xml.pending;

/*-
 * #%L
 * jambeth-xml
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

import java.lang.reflect.Array;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.xml.IReader;

public class ArraySetterCommand extends AbstractObjectCommand
		implements IObjectCommand, IInitializingBean {
	protected Integer index;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertTrue(parent.getClass().isArray(), "Parent has to be an array");
		ParamChecker.assertNotNull(index, "Index");
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	@Override
	public void execute(IReader reader) {
		Object value = objectFuture.getValue();
		Array.set(parent, index, value);
	}
}
