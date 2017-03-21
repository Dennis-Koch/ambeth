package com.koch.ambeth.persistence.jdbc.lob;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import com.koch.ambeth.cache.IParentEntityAware;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.IUnmodifiedInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputSource;
import com.koch.ambeth.stream.chars.ICharacterInputStream;

public class ClobInputSource implements ICharacterInputSource, IParentEntityAware, IUnmodifiedInputSource
{
	protected final ILobInputSourceController lobInputSourceController;

	protected Object parentEntity;

	protected Member member;

	public ClobInputSource(ILobInputSourceController lobInputSourceController)
	{
		this.lobInputSourceController = lobInputSourceController;
	}

	@Override
	public void setParentEntity(Object parentEntity, Member member)
	{
		this.parentEntity = parentEntity;
		this.member = member;
	}

	@Override
	public IInputStream deriveInputStream()
	{
		return lobInputSourceController.deriveInputStream(parentEntity, member);
	}

	@Override
	public ICharacterInputStream deriveCharacterInputStream()
	{
		return lobInputSourceController.deriveCharacterInputStream(parentEntity, member);
	}
}
