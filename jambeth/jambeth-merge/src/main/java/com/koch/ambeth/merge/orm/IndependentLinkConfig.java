package com.koch.ambeth.merge.orm;

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

import com.koch.ambeth.util.ParamChecker;

public class IndependentLinkConfig extends LinkConfig implements ILinkConfig {
	protected Class<?> left, right;

	public IndependentLinkConfig(String alias) {
		ParamChecker.assertParamNotNullOrEmpty(alias, "alias");
		this.alias = alias;
	}

	public Class<?> getLeft() {
		return left;
	}

	public void setLeft(Class<?> left) {
		this.left = left;
	}

	public Class<?> getRight() {
		return right;
	}

	public void setRight(Class<?> right) {
		this.right = right;
	}
}
