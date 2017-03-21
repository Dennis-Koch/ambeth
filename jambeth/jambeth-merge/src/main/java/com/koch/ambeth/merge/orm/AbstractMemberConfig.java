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

public abstract class AbstractMemberConfig implements IMemberConfig {
	private final String name;

	private String definedBy;

	private boolean alternateId;

	private boolean ignore;

	private boolean isTransient;

	private boolean explicitlyNotMergeRelevant;

	public AbstractMemberConfig(String name) {
		ParamChecker.assertParamNotNullOrEmpty(name, "name");
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isAlternateId() {
		return alternateId;
	}

	public void setAlternateId(boolean alternateId) {
		this.alternateId = alternateId;
	}

	@Override
	public boolean isTransient() {
		return isTransient;
	}

	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

	@Override
	public String getDefinedBy() {
		return definedBy;
	}

	public void setDefinedBy(String definedBy) {
		this.definedBy = definedBy;
	}

	@Override
	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	@Override
	public boolean isExplicitlyNotMergeRelevant() {
		return explicitlyNotMergeRelevant;
	}

	public void setExplicitlyNotMergeRelevant(boolean explicitlyNotMergeRelevant) {
		this.explicitlyNotMergeRelevant = explicitlyNotMergeRelevant;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public abstract boolean equals(Object obj);

	public boolean equals(AbstractMemberConfig other) {
		return getName().equals(other.getName());
	}
}
