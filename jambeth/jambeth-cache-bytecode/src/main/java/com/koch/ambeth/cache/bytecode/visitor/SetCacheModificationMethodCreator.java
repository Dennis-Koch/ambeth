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
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.merge.cache.ICacheModification;

public class SetCacheModificationMethodCreator extends ClassGenerator {
	private static final MethodInstance m_callCacheModificationActive =
			new MethodInstance(null, SetCacheModificationMethodCreator.class, void.class,
					"callCacheModificationActive", ICacheModification.class, boolean.class, boolean.class);

	private static final MethodInstance m_callCacheModificationInternalUpdate =
			new MethodInstance(null, SetCacheModificationMethodCreator.class, void.class,
					"callCacheModificationInternalUpdate", ICacheModification.class, boolean.class,
					boolean.class);

	private static final String cacheModificationName = "__CacheModification";

	public static PropertyInstance getCacheModificationPI(ClassGenerator cv) {
		Object bean = getState().getBeanContext().getService(ICacheModification.class);
		PropertyInstance pi = getState().getProperty(cacheModificationName, bean.getClass());
		if (pi != null) {
			return pi;
		}
		return cv.implementAssignedReadonlyProperty(cacheModificationName, bean);
	}

	public static void cacheModificationActive(PropertyInstance p_cacheModification,
			MethodGenerator mg, Script script) {
		final int loc_cacheModification = mg.newLocal(ICacheModification.class);
		final int loc_oldActive = mg.newLocal(boolean.class);

		// ICacheModification cacheModification = this.cacheModification;
		mg.callThisGetter(p_cacheModification);
		mg.storeLocal(loc_cacheModification);

		// boolean oldActive = cacheModification.isActive();
		mg.loadLocal(loc_cacheModification);
		mg.invokeInterface(
				new MethodInstance(null, ICacheModification.class, boolean.class, "isActive"));
		mg.storeLocal(loc_oldActive);

		// callModificationActive(cacheModification, oldActive, true)
		mg.loadLocal(loc_cacheModification);
		mg.loadLocal(loc_oldActive);
		mg.push(true);
		mg.invokeStatic(m_callCacheModificationActive);

		mg.tryFinally(script, new Script() {
			@Override
			public void execute(MethodGenerator mg) {
				// callModificationActive(cacheModification, oldActive, false)
				mg.loadLocal(loc_cacheModification);
				mg.loadLocal(loc_oldActive);
				mg.push(false);
				mg.invokeStatic(m_callCacheModificationActive);
			}
		});
	}

	public static void cacheModificationInternalUpdate(PropertyInstance p_cacheModification,
			MethodGenerator mg, Script script) {
		final int loc_cacheModification = mg.newLocal(ICacheModification.class);
		final int loc_oldInternalUpdate = mg.newLocal(boolean.class);

		// ICacheModification cacheModification = this.cacheModification;
		mg.callThisGetter(p_cacheModification);
		mg.storeLocal(loc_cacheModification);

		// boolean oldInternalUpdate = cacheModification.isInternalUpdate();
		mg.loadLocal(loc_cacheModification);
		mg.invokeInterface(
				new MethodInstance(null, ICacheModification.class, boolean.class, "isInternalUpdate"));
		mg.storeLocal(loc_oldInternalUpdate);

		// callModificationInternalUpdate(cacheModification, oldInternalUpdate, true)
		mg.loadLocal(loc_cacheModification);
		mg.loadLocal(loc_oldInternalUpdate);
		mg.push(true);
		mg.invokeStatic(m_callCacheModificationInternalUpdate);

		mg.tryFinally(script, new Script() {
			@Override
			public void execute(MethodGenerator mg) {
				// callModificationInternalUpdate(cacheModification, oldInternalUpdate, false)
				mg.loadLocal(loc_cacheModification);
				mg.loadLocal(loc_oldInternalUpdate);
				mg.push(false);
				mg.invokeStatic(m_callCacheModificationInternalUpdate);
			}
		});
	}

	public SetCacheModificationMethodCreator(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visitEnd() {
		// force implementation
		getCacheModificationPI(this);

		super.visitEnd();
	}

	public static void callCacheModificationActive(ICacheModification cacheModification,
			boolean oldValue, boolean newValue) {
		if (!oldValue) {
			cacheModification.setActive(newValue);
		}
	}

	public static void callCacheModificationInternalUpdate(ICacheModification cacheModification,
			boolean oldValue, boolean newValue) {
		if (!oldValue) {
			cacheModification.setInternalUpdate(newValue);
		}
	}
}
