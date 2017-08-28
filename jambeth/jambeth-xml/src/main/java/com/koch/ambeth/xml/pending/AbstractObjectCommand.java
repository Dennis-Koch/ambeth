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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;

public abstract class AbstractObjectCommand implements IObjectCommand, IInitializingBean {

	protected IObjectFuture objectFuture;

	protected Object parent;

	public AbstractObjectCommand() {
		// intended blank
	}

	public AbstractObjectCommand(IObjectFuture objectFuture, Object parent) {
		super();
		this.objectFuture = objectFuture;
		this.parent = parent;
	}

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(objectFuture, "ObjectFuture");
		ParamChecker.assertNotNull(parent, "Parent");
	}

	@Override
	public IObjectFuture getObjectFuture() {
		return objectFuture;
	}

	public Object getParent() {
		return parent;
	}
}
