package com.koch.ambeth.merge.mixin;

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

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.StringBuilderUtil;

public class ObjRefMixin
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public boolean objRefEquals(IObjRef objRef, Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!(obj instanceof IObjRef))
		{
			return false;
		}
		IObjRef other = (IObjRef) obj;
		if (objRef.getIdNameIndex() != other.getIdNameIndex() || !objRef.getRealType().equals(other.getRealType()))
		{
			return false;
		}
		Object id = objRef.getId();
		Object otherId = other.getId();
		if (id == null || otherId == null)
		{
			return false;
		}
		if (!id.getClass().isArray() || !otherId.getClass().isArray())
		{
			return id.equals(otherId);
		}
		Object[] idArray = (Object[]) id;
		Object[] otherIdArray = (Object[]) otherId;
		if (idArray.length != otherIdArray.length)
		{
			return false;
		}
		for (int a = idArray.length; a-- > 0;)
		{
			if (!idArray[a].equals(otherIdArray[a]))
			{
				return false;
			}
		}
		return true;
	}

	public int objRefHashCode(IObjRef objRef)
	{
		return objRef.getId().hashCode() ^ objRef.getRealType().hashCode() ^ objRef.getIdNameIndex();
	}

	public void objRefToString(IObjRef objRef, StringBuilder sb)
	{
		sb.append("ObjRef ");
		byte idIndex = objRef.getIdNameIndex();
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			sb.append("PK=");
		}
		else
		{
			sb.append("AK").append(idIndex).append('=');
		}
		StringBuilderUtil.appendPrintable(sb, objRef.getId());
		sb.append(" version=").append(objRef.getVersion()).append(" type=").append(objRef.getRealType().getName());
	}
}
