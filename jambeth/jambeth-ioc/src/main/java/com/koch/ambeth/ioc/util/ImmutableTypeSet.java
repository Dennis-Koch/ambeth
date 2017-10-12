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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.collections.LinkedHashSet;

public class ImmutableTypeSet extends IImmutableTypeSet implements IImmutableTypeExtendable {
	protected static final LinkedHashSet<Class<?>> staticImmutableTypeSet =
			new LinkedHashSet<>(0.5f);

	static {
		staticImmutableTypeSet.add(Integer.class);
		staticImmutableTypeSet.add(Integer.TYPE);
		staticImmutableTypeSet.add(Long.class);
		staticImmutableTypeSet.add(Long.TYPE);
		staticImmutableTypeSet.add(Double.class);
		staticImmutableTypeSet.add(Double.TYPE);
		staticImmutableTypeSet.add(Float.class);
		staticImmutableTypeSet.add(Float.TYPE);
		staticImmutableTypeSet.add(Short.class);
		staticImmutableTypeSet.add(Short.TYPE);
		staticImmutableTypeSet.add(Character.class);
		staticImmutableTypeSet.add(Character.TYPE);
		staticImmutableTypeSet.add(Byte.class);
		staticImmutableTypeSet.add(Byte.TYPE);
		staticImmutableTypeSet.add(Boolean.class);
		staticImmutableTypeSet.add(Boolean.TYPE);
		staticImmutableTypeSet.add(String.class);
		staticImmutableTypeSet.add(Class.class);
		staticImmutableTypeSet.add(void.class);
		staticImmutableTypeSet.add(BigInteger.class);
		staticImmutableTypeSet.add(BigDecimal.class);

		staticImmutableTypeSet.add(Pattern.class);
		staticImmutableTypeSet.add(URI.class);
		staticImmutableTypeSet.add(URL.class);
		staticImmutableTypeSet.add(File.class);

		// java time
		staticImmutableTypeSet.add(Duration.class);
		staticImmutableTypeSet.add(Instant.class);
		staticImmutableTypeSet.add(LocalDate.class);
		staticImmutableTypeSet.add(LocalDateTime.class);
		staticImmutableTypeSet.add(LocalTime.class);
		staticImmutableTypeSet.add(MonthDay.class);
		staticImmutableTypeSet.add(OffsetDateTime.class);
		staticImmutableTypeSet.add(OffsetTime.class);
		staticImmutableTypeSet.add(Period.class);
		staticImmutableTypeSet.add(Year.class);
		staticImmutableTypeSet.add(YearMonth.class);
		staticImmutableTypeSet.add(ZonedDateTime.class);
		staticImmutableTypeSet.add(ZoneOffset.class);

		staticImmutableTypeSet.add(InetAddress.class);
		staticImmutableTypeSet.add(Inet4Address.class);
		staticImmutableTypeSet.add(Inet6Address.class);
	}

	private final ClassExtendableContainer<Class<?>> immutableSuperTypes =
			new ClassExtendableContainer<>("", "");

	public ImmutableTypeSet() {
		immutableSuperTypes.register(Charset.class, Charset.class);
	}

	@Override
	public void addImmutableTypesTo(Collection<Class<?>> collection) {
		collection.addAll(staticImmutableTypeSet);
	}

	@Override
	public boolean isImmutableType(Class<?> type) {
		return type.isPrimitive() || type.isEnum() || staticImmutableTypeSet.contains(type)
				|| IImmutableType.class.isAssignableFrom(type)
				|| immutableSuperTypes.getExtension(type) != null;
	}

	@Override
	public void registerImmutableType(Class<?> immutableType) {
		immutableSuperTypes.register(immutableType, immutableType);
	}

	@Override
	public void unregisterImmutableType(Class<?> immutableType) {
		immutableSuperTypes.unregister(immutableType, immutableType);
	}
}
