package com.koch.ambeth.cache.valueholdercontainer;

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

import com.koch.ambeth.util.annotation.EntityEqualsAspect;
import com.koch.ambeth.util.annotation.FireTargetOnPropertyChange;
import com.koch.ambeth.util.annotation.FireThisOnPropertyChange;

@EntityEqualsAspect
public class MaterialType
{
	private int id;

	private int version;

	private String name;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	@FireTargetOnPropertyChange("Temp2")
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@FireThisOnPropertyChange("Name")
	public String getTemp1()
	{
		return getName() + "$Temp1";
	}

	public String getTemp2()
	{
		return getName() + "$Temp2";
	}
}
