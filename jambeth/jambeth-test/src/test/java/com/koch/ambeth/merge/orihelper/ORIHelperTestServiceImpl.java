package com.koch.ambeth.merge.orihelper;

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

import javax.persistence.PersistenceContext;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.independent.EntityB;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.ParamChecker;

@Service(ORIHelperTestService.class)
@PersistenceContext
public class ORIHelperTestServiceImpl implements ORIHelperTestService, IInitializingBean {
	protected IEntityFactory entityFactory;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(entityFactory, "EntityFactory");
	}

	public void setEntityFactory(IEntityFactory entityFactory) {
		this.entityFactory = entityFactory;
	}

	@Override
	public EntityB[] getAllEntityBs() {
		return new EntityB[] {getE(1, 1), null, getE(2, 1)};
	}

	private EntityB getE(int id, int v) {
		EntityB e = entityFactory.createEntity(EntityB.class);
		e.setId(id);
		e.setVersion(v);
		return e;
	}
}
