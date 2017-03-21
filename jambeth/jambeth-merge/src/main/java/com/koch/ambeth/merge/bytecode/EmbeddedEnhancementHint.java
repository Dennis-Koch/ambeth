package com.koch.ambeth.merge.bytecode;

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

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.ioc.bytecode.ITargetNameEnhancementHint;

public class EmbeddedEnhancementHint implements ITargetNameEnhancementHint, Serializable
{
	private static final long serialVersionUID = -8425591662442655129L;

	public static boolean hasMemberPath(IEnhancementHint hint)
	{
		return getMemberPath(hint) != null;
	}

	public static Class<?> getParentObjectType(IEnhancementHint hint)
	{
		if (hint == null)
		{
			return null;
		}
		EmbeddedEnhancementHint unwrap = hint.unwrap(EmbeddedEnhancementHint.class);
		if (unwrap == null)
		{
			return null;
		}
		return unwrap.getParentObjectType();
	}

	public static Class<?> getRootEntityType(IEnhancementHint hint)
	{
		if (hint == null)
		{
			return null;
		}
		EmbeddedEnhancementHint unwrap = hint.unwrap(EmbeddedEnhancementHint.class);
		if (unwrap == null)
		{
			return null;
		}
		return unwrap.getRootEntityType();
	}

	public static String getMemberPath(IEnhancementHint hint)
	{
		if (hint == null)
		{
			return null;
		}
		EmbeddedEnhancementHint unwrap = hint.unwrap(EmbeddedEnhancementHint.class);
		if (unwrap == null)
		{
			return null;
		}
		return unwrap.getMemberPath();
	}

	private final Class<?> parentObjectType;

	private final Class<?> rootEntityType;

	private final String memberPath;

	public EmbeddedEnhancementHint(Class<?> rootEntityType, Class<?> parentObjectType, String memberPath)
	{
		this.rootEntityType = rootEntityType;
		this.parentObjectType = parentObjectType;
		this.memberPath = memberPath;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (EmbeddedEnhancementHint.class.isAssignableFrom(includedContextType))
		{
			return (T) this;
		}
		return null;
	}

	public Class<?> getParentObjectType()
	{
		return parentObjectType;
	}

	public Class<?> getRootEntityType()
	{
		return rootEntityType;
	}

	public String getMemberPath()
	{
		return memberPath;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof EmbeddedEnhancementHint))
		{
			return false;
		}
		EmbeddedEnhancementHint other = (EmbeddedEnhancementHint) obj;
		return getParentObjectType().equals(other.getParentObjectType()) && getMemberPath().equals(other.getMemberPath());
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getParentObjectType().hashCode() ^ getMemberPath().hashCode();
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		if (getMemberPath() != null && getMemberPath().length() > 0)
		{
			return typeToEnhance.getName() + "_" + getMemberPath();
		}
		return typeToEnhance.getName();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " Root: " + getRootEntityType().getSimpleName() + ", Path: " + getMemberPath() + ", Parent: "
				+ getParentObjectType().getSimpleName();
	}
}
