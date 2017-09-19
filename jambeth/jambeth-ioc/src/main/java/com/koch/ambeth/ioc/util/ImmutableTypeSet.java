package com.koch.ambeth.ioc.util;

/*-
 * #%L
 * jambeth-ioc
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

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Collection;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.collections.LinkedHashSet;

public final class ImmutableTypeSet {
	protected static final LinkedHashSet<Class<?>> immutableTypeSet =
			new LinkedHashSet<>(0.5f);

	private static final ClassExtendableContainer<Class<?>> immutableSuperTypes =
			new ClassExtendableContainer<>("", "");

	static {
		immutableTypeSet.add(Integer.class);
		immutableTypeSet.add(Integer.TYPE);
		immutableTypeSet.add(Long.class);
		immutableTypeSet.add(Long.TYPE);
		immutableTypeSet.add(Double.class);
		immutableTypeSet.add(Double.TYPE);
		immutableTypeSet.add(Float.class);
		immutableTypeSet.add(Float.TYPE);
		immutableTypeSet.add(Short.class);
		immutableTypeSet.add(Short.TYPE);
		immutableTypeSet.add(Character.class);
		immutableTypeSet.add(Character.TYPE);
		immutableTypeSet.add(Byte.class);
		immutableTypeSet.add(Byte.TYPE);
		immutableTypeSet.add(Boolean.class);
		immutableTypeSet.add(Boolean.TYPE);
		immutableTypeSet.add(String.class);
		immutableTypeSet.add(Class.class);
		immutableTypeSet.add(void.class);
		immutableTypeSet.add(BigInteger.class);
		immutableTypeSet.add(BigDecimal.class);

		immutableTypeSet.add(Pattern.class);
		immutableTypeSet.add(URI.class);
		immutableTypeSet.add(URL.class);
		immutableTypeSet.add(File.class);
		immutableTypeSet.add(Instant.class);

		immutableSuperTypes.register(Charset.class, Charset.class);
	}

	public static void addImmutableTypesTo(Collection<Class<?>> collection) {
		collection.addAll(immutableTypeSet);
	}

	public static boolean isImmutableType(Class<?> type) {
		return type.isPrimitive() || type.isEnum() || immutableTypeSet.contains(type)
				|| IImmutableType.class.isAssignableFrom(type)
				|| immutableSuperTypes.getExtension(type) != null;
	}

	public static void flushState() {
		immutableSuperTypes.clearWeakCache();
	}

	private ImmutableTypeSet() {
		// Intended blank
	}
}
