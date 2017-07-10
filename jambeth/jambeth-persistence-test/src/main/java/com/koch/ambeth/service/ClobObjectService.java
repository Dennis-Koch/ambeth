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
import com.koch.ambeth.merge.proxy.MergeContext;
import com.koch.ambeth.model.ClobObject;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.ParamChecker;

@MergeContext
public class ClobObjectService implements IClobObjectService, IInitializingBean, IStartingBean {
	protected static final String param = "ids";

	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<ClobObject> getClobObjectsQuery;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(queryBuilderFactory, "queryBuilderFactory");
	}

	@Override
	public void afterStarted() throws Throwable {
		IQueryBuilder<ClobObject> qb = queryBuilderFactory.create(ClobObject.class);
		getClobObjectsQuery = qb.build(qb.isIn(qb.property("Id"), qb.valueName(param)));
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory queryBuilderFactory) {
		this.queryBuilderFactory = queryBuilderFactory;
	}

	@Override
	public List<ClobObject> getClobObjects(Integer... id) {
		return getClobObjectsQuery.param(param, id).retrieve();
	}

	@Override
	public void updateClobObject(ClobObject clobObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteClobObject(ClobObject clobObject) {
		throw new UnsupportedOperationException();
	}
}
