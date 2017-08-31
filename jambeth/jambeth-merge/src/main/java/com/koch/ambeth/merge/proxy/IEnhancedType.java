package com.koch.ambeth.merge.proxy;

import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;

/*-
 * #%L
 * jambeth-merge
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

/**
 * Marker interface for any type that has been generated via the Ambeth Bytecode library at runtime
 */
public interface IEnhancedType {
	/**
	 * Returns the original type that has been "derived" from. The original type may be a class or an
	 * interface and is not necessarily a super type if this object (though in nearly all cases this
	 * is true).
	 *
	 * @return The original contextual type that has been passed to
	 *         {@link IBytecodeEnhancer#getEnhancedType(Class, com.koch.ambeth.ioc.bytecode.IEnhancementHint)}
	 *         during enhancement. In most cases this contextual type is an ordinary "static class" -
	 *         with existing readable source code in your SCM.
	 */
	Class<?> get__BaseType();
}
