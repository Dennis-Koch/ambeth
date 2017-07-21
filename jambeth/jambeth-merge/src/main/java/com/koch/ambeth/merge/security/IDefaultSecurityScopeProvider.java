package com.koch.ambeth.merge.security;

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

import com.koch.ambeth.service.model.ISecurityScope;

/**
 * Allows to completely customize how and which security scopes will be resolved whenever they are
 * not explicitly set for the current thread
 */
public interface IDefaultSecurityScopeProvider {
	/**
	 * Returns the resolved default security scopes. This method is called by the framework
	 * (implemented by {@link SecurityScopeProvider#getSecurityScopes()}) whenever it is requested to
	 * resolve security scopes and they are not explicitly set for the current calling thread. You can
	 * explicitly set thread-local security scopes by calling
	 * {@link ISecurityScopeProvider#pushSecurityScopes(ISecurityScope, com.koch.ambeth.util.state.IStateRollback...)}.
	 *
	 * @return The resolved default security scopes
	 */
	ISecurityScope[] getDefaultSecurityScopes();
}
