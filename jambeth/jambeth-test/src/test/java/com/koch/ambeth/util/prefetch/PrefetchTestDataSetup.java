package com.koch.ambeth.util.prefetch;

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

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.merge.util.setup.AbstractDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilder;

public class PrefetchTestDataSetup extends AbstractDatasetBuilder {
	public EntityA rootEntityA;

	public EntityD rootEntityD;

	@Override
	public Collection<Class<? extends IDatasetBuilder>> getDependsOn() {
		return null;
	}

	@Override
	protected void buildDatasetInternal() {
		EntityA entityA1 = createEntity(EntityA.class);
		EntityA entityA2 = createEntity(EntityA.class);
		EntityA entityA3 = createEntity(EntityA.class);
		EntityA entityA4 = createEntity(EntityA.class);

		EntityB entityB1 = createEntity(EntityB.class);
		EntityB entityB2 = createEntity(EntityB.class);
		EntityB entityB3 = createEntity(EntityB.class);
		EntityB entityB4 = createEntity(EntityB.class);

		EntityC entityC1 = createEntity(EntityC.class);
		EntityC entityC2 = createEntity(EntityC.class);
		EntityC entityC3 = createEntity(EntityC.class);

		entityA1.getBsOfA().add(entityB1);
		entityA2.getBsOfA().add(entityB2);
		entityA3.getBsOfA().add(entityB3);
		entityA4.getBsOfA().add(entityB4);

		entityA1.getCsOfA().add(entityC1);
		entityA2.getCsOfA().add(entityC2);
		entityA3.getCsOfA().add(entityC3);
		//
		// entityB1.getCsOfB().add(entityC1);
		// entityB2.getCsOfB().add(entityC2);
		// entityB3.getCsOfB().add(entityC3);
		//
		entityA1.getAsOfA().add(entityA2);
		entityA2.getAsOfA().add(entityA3);

		entityB1.getAsOfB().add(entityA4);

		EntityD entityD1 = createEntity(EntityD.class);
		EntityD entityD2 = createEntity(EntityD.class);
		EntityD entityD3 = createEntity(EntityD.class);
		EntityD entityD4 = createEntity(EntityD.class);
		EntityD entityD5 = createEntity(EntityD.class);
		EntityD entityD6 = createEntity(EntityD.class);

		entityD1.getDsOfD().add(entityD2);
		entityD2.getDsOfD().add(entityD3);
		entityD3.getDsOfD().add(entityD4);
		entityD4.getDsOfD().add(entityD5);
		entityD5.getDsOfD().add(entityD6);
		//
		// entityB1.getBsOfB().add(createEntity(EntityB.class));
		// entityB2.getBsOfB().add(createEntity(EntityB.class));
		// entityB3.getBsOfB().add(createEntity(EntityB.class));

		rootEntityA = entityA1;
		rootEntityD = entityD1;

		// int count = 2;
		// ArrayList<EntityA> allA = new ArrayList<EntityA>();
		// ArrayList<EntityB> allB = new ArrayList<EntityB>();
		// ArrayList<EntityC> allC = new ArrayList<EntityC>();
		// for (int a = 0; a < count; a++)
		// {
		// EntityA entityA = createEntity(EntityA.class);
		// allA.add(entityA);
		// }
		// for (int a = 0; a < count; a++)
		// {
		// EntityB entityB = createEntity(EntityB.class);
		// allB.add(entityB);
		// }
		// for (int a = 0; a < count; a++)
		// {
		// EntityC entityC = createEntity(EntityC.class);
		// allC.add(entityC);
		// }
		// for (int a = 0; a < count; a++)
		// {
		// EntityA entityA = allA.get(a);
		// EntityA parentEntityA = nextOfIndex(allA, a);
		// EntityB entityB = allB.get(a);
		// EntityC entityC = allC.get(a);
		// addIfNotExists(entityA.getEntityBs(), entityB);
		// addIfNotExists(entityA.getEntityCs(), entityC);
		// if (parentEntityA.getParentA() != entityA)
		// {
		// addIfNotExists(parentEntityA.getEntityAs(), entityA);
		// entityA.setParentA(parentEntityA);
		// }
		// }
		// for (int a = 0; a < count; a++)
		// {
		// EntityB entityB = allB.get(a);
		// EntityB parentEntityB = nextOfIndex(allB, a);
		// EntityC entityC = allC.get(a);
		// addIfNotExists(entityB.getEntityCs(), entityC);
		// if (parentEntityB.getParentB() != entityB)
		// {
		// addIfNotExists(parentEntityB.getEntityBs(), entityB);
		// entityB.setParentB(parentEntityB);
		// }
		// }
		// for (int a = 0; a < count; a++)
		// {
		// EntityC entityC = allC.get(a);
		// EntityC parentEntityC = nextOfIndex(allC, a);
		// if (parentEntityC.getParentC() != entityC)
		// {
		// addIfNotExists(parentEntityC.getEntityCs(), entityC);
		// entityC.setParentC(parentEntityC);
		// }
		// }
	}

	protected <T> T nextOfIndex(List<T> list, int index) {
		return list.get((index + 1) % list.size());
	}

	protected <T> void addIfNotExists(List<T> list, T item) {
		if (list.contains(item)) {
			return;
		}
		list.add(item);
	}

	protected <T> T selectRandom(List<T> list) {
		return list.get((int) (Math.random() * list.size()));
	}
}
