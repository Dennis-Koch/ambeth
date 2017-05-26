package com.koch.ambeth.informationbus.persistence.datagenerator;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
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

import java.util.HashMap;
import java.util.Map;

import com.koch.ambeth.informationbus.persistence.datagenerator.setter.StringTestSetter;

public class EntityData {

	public static final String[] TECHNICAL_ATTRIBUTES =
			new String[] {"Id", "ID", "CreatedBy", "CreatedOn", "UpdatedBy", "UpdatedOn", "Version"};

	public static String simpleBuidExt(int id) {
		return String.format("%06d", id);
	}

	public static String simpleBuid(int id) {
		return "BUID" + simpleBuidExt(id);
	}

	/**
	 * Business Object buid is different from DService buid in its PropertyName, so two different
	 * methods.
	 *
	 * @param id
	 * @return "Buid" + id in normalized form
	 */
	public static String simpleBOBuid(int id) {
		return "Buid" + simpleBuidExt(id);
	}

	public static Map<Object, Object> getSimpleAttributes(int index) {
		Map<Object, Object> attributes = new HashMap<>();
		attributes.put(StringTestSetter.class, simpleBuidExt(index));
		return attributes;
	}
}
