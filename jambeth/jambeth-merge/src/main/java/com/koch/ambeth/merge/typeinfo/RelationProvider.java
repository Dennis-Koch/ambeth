package com.koch.ambeth.merge.typeinfo;

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

import java.util.Arrays;

import javax.persistence.Embeddable;

import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.collections.SmartCopySet;
import com.koch.ambeth.util.typeinfo.INoEntityTypeExtendable;
import com.koch.ambeth.util.typeinfo.IRelationProvider;

public class RelationProvider implements IRelationProvider, INoEntityTypeExtendable {
	protected final SmartCopySet<Class<?>> primitiveTypes = new SmartCopySet<>();

	protected final ClassExtendableContainer<Boolean> noEntityTypeExtendables =
			new ClassExtendableContainer<>("flag", "noEntityType");

	public RelationProvider() {
		ImmutableTypeSet.addImmutableTypesTo(primitiveTypes);

		primitiveTypes.addAll(
				Arrays.asList(new Class<?>[] {Object.class, java.util.Date.class, java.sql.Date.class,
						java.sql.Timestamp.class, java.util.Calendar.class, java.lang.Integer.class,
						java.lang.Long.class, java.lang.Double.class, java.lang.Float.class,
						java.lang.Short.class, java.lang.Character.class, java.lang.Byte.class}));
		primitiveTypes.add(java.util.GregorianCalendar.class);
		primitiveTypes.add(javax.xml.datatype.XMLGregorianCalendar.class);
	}

	@Override
	public boolean isEntityType(Class<?> type) {
		if (type == null || type.isPrimitive() || type.isEnum() || primitiveTypes.contains(type)
				|| Boolean.TRUE == noEntityTypeExtendables.getExtension(type)) {
			return false;
		}
		if (type.isAnnotationPresent(Embeddable.class) || IImmutableType.class.isAssignableFrom(type)) {
			return false;
		}
		return true;
	}

	@Override
	public void registerNoEntityType(Class<?> noEntityType) {
		noEntityTypeExtendables.register(Boolean.TRUE, noEntityType);
	}

	@Override
	public void unregisterNoEntityType(Class<?> noEntityType) {
		noEntityTypeExtendables.unregister(Boolean.TRUE, noEntityType);
	}
}
