package com.koch.ambeth.util.typeinfo;

/*-
 * #%L
 * jambeth-util
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

import java.lang.annotation.Annotation;
import java.util.Collection;

public final class NullTypeInfoItem implements ITypeInfoItem
{
	public static final NullTypeInfoItem INSTANCE = new NullTypeInfoItem();

	private NullTypeInfoItem()
	{
		// Intended blank
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public Object getDefaultValue()
	{
		return null;
	}

	@Override
	public void setDefaultValue(Object defaultValue)
	{
	}

	@Override
	public Object getNullEquivalentValue()
	{
		return null;
	}

	@Override
	public void setNullEquivalentValue(Object nullEquivalentValue)
	{
	}

	@Override
	public Class<?> getRealType()
	{
		return null;
	}

	@Override
	public Class<?> getElementType()
	{
		return null;
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return null;
	}

	@Override
	public boolean canRead()
	{
		return false;
	}

	@Override
	public boolean canWrite()
	{
		return false;
	}

	@Override
	public boolean isTechnicalMember()
	{
		return false;
	}

	@Override
	public void setTechnicalMember(boolean b)
	{
	}

	@Override
	public Object getValue(Object obj)
	{
		return null;
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		return null;
	}

	@Override
	public void setValue(Object obj, Object value)
	{
	}

	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		return null;
	}

	@Override
	public String getXMLName()
	{
		return null;
	}

	@Override
	public boolean isXMLIgnore()
	{
		return false;
	}

	@Override
	public Collection<?> createInstanceOfCollection()
	{
		return null;
	}
}
