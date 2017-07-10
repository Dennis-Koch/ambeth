package com.koch.ambeth.service;

/*-
 * #%L
 * jambeth-persistence-test
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

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.ParamChecker;

@Service(IMaterialService.class)
@PersistenceContext
public class MaterialService implements IInitializingBean, IStartingBean, IMaterialService {
	private static final String QueryParamKey = "Key";

	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<Material> getMaterialByIdQuery;

	protected IQuery<Material> getAllMaterialsQuery;

	protected IQuery<Material> getMaterialByNameQuery;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(queryBuilderFactory, "QueryBuilderFactory");
	}

	@Override
	public void afterStarted() throws Throwable {
		IQueryBuilder<Material> getMaterialQB = queryBuilderFactory.create(Material.class);
		getMaterialByIdQuery = getMaterialQB.build(getMaterialQB.isEqualTo(getMaterialQB.property("Id"),
				getMaterialQB.valueName(QueryParamKey)));

		IQueryBuilder<Material> getAllMaterialsQB = queryBuilderFactory.create(Material.class);
		getAllMaterialsQuery = getAllMaterialsQB.build();

		IQueryBuilder<Material> getMaterialByNameQB = queryBuilderFactory.create(Material.class);
		getMaterialByNameQuery = getMaterialByNameQB.build(getMaterialByNameQB.isEqualTo(
				getMaterialByNameQB.property("Name"), getMaterialByNameQB.valueName(QueryParamKey)));
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory queryBuilderFactory) {
		this.queryBuilderFactory = queryBuilderFactory;
	}

	@Override
	public Material getMaterial(int id) {
		return getMaterialByIdQuery.param(QueryParamKey, id).retrieveSingle();
	}

	@Override
	public List<Material> getAllMaterials() {
		return getAllMaterialsQuery.retrieve();
	}

	@Override
	public Material getMaterialByName(String name) {
		return getMaterialByNameQuery.param(QueryParamKey, name).retrieveSingle();
	}

	@Override
	public void updateMaterial(Material material) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateMaterials(Material[] materials) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteMaterial(Material material) {
		throw new UnsupportedOperationException();
	}
}
