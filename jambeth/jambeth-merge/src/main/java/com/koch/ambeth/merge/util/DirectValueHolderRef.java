package com.koch.ambeth.merge.util;

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

import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.service.metadata.RelationMember;

public class DirectValueHolderRef
{
	protected final IObjRefContainer vhc;

	protected final RelationMember member;

	protected final boolean objRefsOnly;

	public DirectValueHolderRef(IObjRefContainer vhc, RelationMember member)
	{
		this.vhc = vhc;
		this.member = member;
		this.objRefsOnly = false;
	}

	public DirectValueHolderRef(IObjRefContainer vhc, RelationMember member, boolean objRefsOnly)
	{
		this.vhc = vhc;
		this.member = member;
		this.objRefsOnly = objRefsOnly;
	}

	public IObjRefContainer getVhc()
	{
		return vhc;
	}

	public RelationMember getMember()
	{
		return member;
	}

	public boolean isObjRefsOnly()
	{
		return objRefsOnly;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof DirectValueHolderRef))
		{
			return false;
		}
		DirectValueHolderRef other = (DirectValueHolderRef) obj;
		return vhc == other.vhc && member == other.member;
	}

	@Override
	public int hashCode()
	{
		return vhc.hashCode() ^ member.hashCode();
	}
}
