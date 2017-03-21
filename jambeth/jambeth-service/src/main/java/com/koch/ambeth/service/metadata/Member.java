package com.koch.ambeth.service.metadata;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.ioc.accessor.AbstractAccessor;

public abstract class Member extends AbstractAccessor implements Comparable<Member>
{
	public abstract Class<?> getElementType();

	public abstract Class<?> getDeclaringType();

	public abstract Class<?> getRealType();

	public abstract Class<?> getEntityType();

	public abstract boolean isToMany();

	public abstract Object getNullEquivalentValue();

	public abstract <V extends Annotation> V getAnnotation(Class<V> annotationType);

	public abstract String getName();

	@Override
	public int compareTo(Member o)
	{
		return getName().compareTo(o.getName());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Member))
		{
			return false;
		}
		Member other = (Member) obj;
		return getEntityType().equals(other.getEntityType()) && getName().equals(other.getName());
	}

	@Override
	public int hashCode()
	{
		return getEntityType().hashCode() ^ getName().hashCode();
	}

	@Override
	public String toString()
	{
		return "Member " + getName();
	}
}
