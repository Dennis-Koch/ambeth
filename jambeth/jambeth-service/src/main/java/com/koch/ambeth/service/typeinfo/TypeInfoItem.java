package com.koch.ambeth.service.typeinfo;

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

import java.util.Collection;
import java.util.Set;

import com.koch.ambeth.util.collections.ObservableArrayList;
import com.koch.ambeth.util.collections.ObservableHashSet;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.typeinfo.FastConstructorAccess;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

public abstract class TypeInfoItem implements ITypeInfoItem {
	public static void setEntityType(Class<?> entityType, ITypeInfoItem member,
			IProperties properties) {
		if (member instanceof TypeInfoItem) {
			((TypeInfoItem) member).setElementType(entityType);
		}
		else {
			throw new IllegalStateException("TypeInfoItem not supported: " + member);
		}
	}

	protected Class<?> elementType;

	protected Class<?> declaringType;

	protected boolean technicalMember;

	@Override
	public Class<?> getElementType() {
		return elementType;
	}

	public void setElementType(Class<?> elementType) {
		this.elementType = elementType;
	}

	@Override
	public Class<?> getDeclaringType() {
		return declaringType;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public boolean isTechnicalMember() {
		return technicalMember;
	}

	@Override
	public void setTechnicalMember(boolean technicalMember) {
		this.technicalMember = technicalMember;
	}

	@Override
	public abstract void setNullEquivalentValue(Object nullEquivalentValue);

	protected abstract FastConstructorAccess<?> getConstructorOfRealType();

	@Override
	public Collection<?> createInstanceOfCollection() {
		// OneToMany or ManyToMany Relationship
		Class<?> realType = getRealType();
		if (Iterable.class.isAssignableFrom(realType)) {
			if (realType.isInterface()) {
				if (Set.class.isAssignableFrom(realType)) {
					return new ObservableHashSet<>();
				}
				return new ObservableArrayList<>();
			}
			try {
				return (Collection<?>) getConstructorOfRealType().newInstance();
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return null;
	}
}
