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

public class LinkConfig implements ILinkConfig
{
	protected String source;

	protected String alias;

	protected CascadeDeleteDirection cascadeDeleteDirection = CascadeDeleteDirection.NONE;

	public LinkConfig(String source)
	{
		ParamChecker.assertParamNotNull(source, "source");
		this.source = source;
	}

	protected LinkConfig()
	{
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	@Override
	public CascadeDeleteDirection getCascadeDeleteDirection()
	{
		return cascadeDeleteDirection;
	}

	public void setCascadeDeleteDirection(CascadeDeleteDirection cascadeDeleteDirection)
	{
		this.cascadeDeleteDirection = cascadeDeleteDirection;
	}
}
