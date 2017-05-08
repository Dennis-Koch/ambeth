package com.koch.ambeth;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.Unit;

public class ObjectMother {
	public static Material getNewMaterial(IEntityFactory entityFactory, Integer id, Integer version,
			String name) {
		Material material = entityFactory.createEntity(Material.class);
		if (id != null) {
			material.setId(id);
		}
		if (version != null) {
			material.setVersion(version.shortValue());
		}
		material.setName(name);
		return material;
	}

	public static Unit getNewUnit(IEntityFactory entityFactory, Integer id, Integer version,
			String name) {
		Unit unit = entityFactory.createEntity(Unit.class);
		if (id != null) {
			unit.setId(id);
		}
		if (version != null) {
			unit.setVersion(version.shortValue());
		}
		unit.setName(name);
		return unit;
	}
}
