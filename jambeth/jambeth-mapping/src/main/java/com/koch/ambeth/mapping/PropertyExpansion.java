package com.koch.ambeth.mapping;

/*-
 * #%L
 * jambeth-mapping
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

import java.util.Arrays;

import com.koch.ambeth.ioc.accessor.AbstractAccessor;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.model.IDataObject;

public class PropertyExpansion extends AbstractAccessor
{
	private final Member[] memberPath;

	private final IEntityMetaData[] metaDataPath;

	public PropertyExpansion(Member[] memberPath, IEntityMetaData[] metaDataPath)
	{
		this.memberPath = memberPath;
		this.metaDataPath = metaDataPath;
	}

	@Override
	public boolean canRead()
	{
		return true;
	}

	@Override
	public boolean canWrite()
	{
		return false;
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		// TODO: What is a NullEqivalentValue?
		return getValue(obj);
	}

	@Override
	public Object getValue(Object obj)
	{
		if (obj == null)
		{
			return null;
		}

		for (Member member : memberPath)
		{
			obj = member.getValue(obj);
			if (obj == null)
			{
				return null;
			}
		}
		return obj;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		// if target object is null it is an error
		if (obj == null || memberPath == null || memberPath.length == 0)
		{
			throw new NullPointerException("target object was null or the memberPath was invaldi");
		}
		Object targetObj = obj;

		// travel down the path to the last element of the path
		for (int a = 0, size = memberPath.length - 1; a < size; a++)
		{
			Member member = memberPath[a];
			Object entity = member.getValue(targetObj);
			if (entity == null)
			{
				// get meta data for next target to create
				IEntityMetaData entityMetaData = metaDataPath[a];
				// last element, or now meta data available
				if (entityMetaData == null)
				{
					throw new IllegalStateException(
							"Must never happen, because there is a next member, and that means that the current targetMember must have meta data: '"
									+ Arrays.toString(memberPath) + "'");
				}
				entity = entityMetaData.newInstance();
				member.setValue(targetObj, entity);
			}
			targetObj = entity;
		}
		Member lastMember = memberPath[memberPath.length - 1];
		// if we are here, then obj is the last element
		if (!EqualsUtil.equals(lastMember.getValue(targetObj), value))
		{
			lastMember.setValue(targetObj, value);
			IDataObject dObj = (IDataObject) targetObj;
			// FIXME: this hack tells the merge process that "we did something here"
			dObj.setToBeUpdated(true);
		}
	}
}
