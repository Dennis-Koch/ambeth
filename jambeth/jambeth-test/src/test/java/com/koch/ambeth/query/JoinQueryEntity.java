package com.koch.ambeth.query;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.model.AbstractEntity;

public class JoinQueryEntity extends AbstractEntity
{
	protected JoinQueryEntity parent;

	protected int joinValue1;

	protected int joinValue2;

	protected JoinQueryEntity()
	{
		// Intended blank
	}

	public JoinQueryEntity getParent()
	{
		return parent;
	}

	public void setParent(JoinQueryEntity parent)
	{
		this.parent = parent;
	}

	public int getJoinValue1()
	{
		return joinValue1;
	}

	public void setJoinValue1(int joinValue1)
	{
		this.joinValue1 = joinValue1;
	}

	public int getJoinValue2()
	{
		return joinValue2;
	}

	public void setJoinValue2(int joinValue2)
	{
		this.joinValue2 = joinValue2;
	}
}
