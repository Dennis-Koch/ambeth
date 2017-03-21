package com.koch.ambeth.cache.bytecode.util;

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

import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;

public class EntityUtil {
	public static Class<?> getEntityType(IEnhancementHint hint) {
		EntityEnhancementHint entityEnhancementHint = hint.unwrap(EntityEnhancementHint.class);
		if (entityEnhancementHint != null) {
			return BytecodeBehaviorState.getState().getOriginalType();
		}
		EmbeddedEnhancementHint embeddedEnhancementHint = hint.unwrap(EmbeddedEnhancementHint.class);
		if (embeddedEnhancementHint != null) {
			return embeddedEnhancementHint.getRootEntityType();
		}
		return null;
	}
}
