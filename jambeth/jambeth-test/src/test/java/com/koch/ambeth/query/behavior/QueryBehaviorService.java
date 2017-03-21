package com.koch.ambeth.query.behavior;

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

import org.junit.Assert;

import com.koch.ambeth.cache.annotation.QueryBehavior;
import com.koch.ambeth.cache.annotation.QueryBehaviorType;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.proxy.Service;

@Service(IQueryBehaviorService.class)
public class QueryBehaviorService implements IStartingBean, IQueryBehaviorService
{
	private static final String QueryParamKey = "Key";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<Material> getMaterialByIdQuery;

	protected IQuery<Material> getAllMaterialsQuery;

	protected IQuery<Material> getMaterialByNameQuery;

	@Override
	public void afterStarted() throws Throwable
	{
		IQueryBuilder<Material> getMaterialQB = queryBuilderFactory.create(Material.class);
		getMaterialByIdQuery = getMaterialQB.build(getMaterialQB.isEqualTo(getMaterialQB.property("Id"), getMaterialQB.valueName(QueryParamKey)));

		IQueryBuilder<Material> getAllMaterialsQB = queryBuilderFactory.create(Material.class);
		getAllMaterialsQuery = getAllMaterialsQB.build();

		IQueryBuilder<Material> getMaterialByNameQB = queryBuilderFactory.create(Material.class);
		getMaterialByNameQuery = getMaterialByNameQB.build(getMaterialByNameQB.isEqualTo(getMaterialByNameQB.property("Name"),
				getMaterialByNameQB.valueName(QueryParamKey)));
	}

	@Override
	public Material getMaterialByName(String name)
	{
		return getMaterialByNameQuery.param(QueryParamKey, name).retrieveSingle();
	}

	@Override
	@QueryBehavior(QueryBehaviorType.OBJREF_ONLY)
	public Material getMaterialByNameObjRefMode(String name)
	{
		Material material = getMaterialByName(name);
		Assert.assertNull(material);
		return material;
	}

	@Override
	@QueryBehavior(QueryBehaviorType.DEFAULT)
	public Material getMaterialByNameDefaultMode(String name)
	{
		Material material = getMaterialByName(name);
		Assert.assertNotNull(material);
		return material;
	}
}
