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

import java.util.List;

public class EmbeddedMaterial
{
	private String name;

	private List<String> names;

	private MaterialType embMatType;

	private EmbeddedMaterial2 embMat2;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getNames()
	{
		return names;
	}

	public void setNames(List<String> names)
	{
		this.names = names;
	}

	public MaterialType getEmbMatType()
	{
		return embMatType;
	}

	public void setEmbMatType(MaterialType embMatType)
	{
		this.embMatType = embMatType;
	}

	public EmbeddedMaterial2 getEmbMat2()
	{
		return embMat2;
	}

	public void setEmbMat2(EmbeddedMaterial2 embMat2)
	{
		this.embMat2 = embMat2;
	}
}
