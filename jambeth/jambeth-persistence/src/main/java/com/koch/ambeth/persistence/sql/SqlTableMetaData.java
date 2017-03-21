package com.koch.ambeth.persistence.sql;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.TableMetaData;
import com.koch.ambeth.util.ParamChecker;

public class SqlTableMetaData extends TableMetaData
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String fullqualifiedEscapedName;

	protected Object initialVersion;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(initialVersion, "initialVersion");
		ParamChecker.assertNotNull(fullqualifiedEscapedName, "fullqualifiedEscapedName");
	}

	public Object getInitialVersion()
	{
		return initialVersion;
	}

	public void setInitialVersion(Object initialVersion)
	{
		this.initialVersion = initialVersion;
	}

	@Override
	public String getFullqualifiedEscapedName()
	{
		return fullqualifiedEscapedName;
	}

	public void setFullqualifiedEscapedName(String fullqualifiedEscapedName)
	{
		this.fullqualifiedEscapedName = fullqualifiedEscapedName;
	}
}
