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
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractMemberConfig implements IMemberConfig {
	@Getter
    private final String name;

    @Getter
    @Setter
	private String definedBy;

    @Getter
    @Setter
	private boolean alternateId;

    @Getter
    @Setter
	private boolean ignore;

    @Getter
    @Setter
	private boolean isTransient;

    @Getter
    @Setter
    private boolean isInterning;

    @Getter
    @Setter
    private String interningBeanName;

    @Getter
    @Setter
	private boolean explicitlyNotMergeRelevant;

	public AbstractMemberConfig(String name) {
		ParamChecker.assertParamNotNullOrEmpty(name, "name");
		this.name = name;
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
