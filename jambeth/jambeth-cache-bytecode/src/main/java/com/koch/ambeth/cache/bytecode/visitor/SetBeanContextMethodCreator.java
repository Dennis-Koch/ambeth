package com.koch.ambeth.cache.bytecode.visitor;

/*-
 * #%L
 * jambeth-cache-bytecode
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

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.ioc.IServiceContext;

public class SetBeanContextMethodCreator extends ClassGenerator {
	private static final String beanContextName = "$beanContext";

	public static PropertyInstance getBeanContextPI(ClassGenerator cv) {
		Object bean = getState().getBeanContext().getService(IServiceContext.class);
		PropertyInstance pi = getState().getProperty(beanContextName, bean.getClass());
		if (pi != null) {
			return pi;
		}
		return cv.implementAssignedReadonlyProperty(beanContextName, bean);
	}

	public SetBeanContextMethodCreator(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visitEnd() {
		// force implementation
		getBeanContextPI(this);

		super.visitEnd();
	}
}
