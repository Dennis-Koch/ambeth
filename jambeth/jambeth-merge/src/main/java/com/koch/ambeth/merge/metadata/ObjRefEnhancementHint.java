package com.koch.ambeth.merge.metadata;

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

import java.io.Serializable;

import org.objectweb.asm.Type;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.ioc.bytecode.ITargetNameEnhancementHint;
import com.koch.ambeth.merge.transfer.ObjRef;

public class ObjRefEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint, Serializable
{
	private static final long serialVersionUID = 4547369981492803630L;

	protected final Class<?> entityType;

	protected final int idIndex;

	public ObjRefEnhancementHint(Class<?> entityType, int idIndex)
	{
		this.entityType = entityType;
		this.idIndex = idIndex;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public int getIdIndex()
	{
		return idIndex;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!getClass().equals(obj.getClass()))
		{
			return false;
		}
		ObjRefEnhancementHint other = (ObjRefEnhancementHint) obj;
		return getEntityType().equals(other.getEntityType()) && idIndex == other.getIdIndex();
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getEntityType().hashCode() ^ getIdIndex();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType)
	{
		if (ObjRefEnhancementHint.class.equals(includedHintType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		return Type.getInternalName(entityType) + "$" + ObjRef.class.getSimpleName() + "$" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "AK" + idIndex);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": " + entityType.getSimpleName() + "-" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "AK" + idIndex);
	}
}
