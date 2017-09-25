package com.koch.ambeth.query.isin;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.proxy.Service;

@Service(IChildService.class)
@PersistenceContext
public class ChildService implements IChildService {
	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Override
	public void searchForParentWithEquals(int parentId) {
		IQueryBuilder<Child> qb = queryBuilderFactory.create(Child.class);
		qb.selectProperty("Parent.Id");
		qb.selectProperty("Id");
		qb.selectProperty("Version");
		IQuery<Child> query = qb.build(qb.let(qb.property("Parent.Id")).isEqualTo(qb.value(parentId)));
		query.retrieveAsData().dispose();
	}

	@Override
	public void getForParentWithIsIn(int... parentIds) {
		IQueryBuilder<Child> qb = queryBuilderFactory.create(Child.class);
		qb.selectProperty("Parent.Id");
		qb.selectProperty("Id");
		qb.selectProperty("Version");
		IQuery<Child> query = qb.build(qb.let(qb.property("Parent.Id")).isIn(qb.value(parentIds)));
		query.retrieveAsData().dispose();
	}
}
