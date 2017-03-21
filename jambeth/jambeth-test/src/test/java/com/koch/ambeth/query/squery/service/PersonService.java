package com.koch.ambeth.query.squery.service;

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

import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.proxy.MergeContext;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.squery.ISquery;
import com.koch.ambeth.query.squery.model.Person;
import com.koch.ambeth.service.proxy.Service;

@Service(IPersonService.class)
@MergeContext
public abstract class PersonService implements IPersonService, ISquery<Person> {
	public static final String CONCRETE_METHOD_RETURN_VALUE = "this method will not be intercepted";

	@Autowired
	protected IQueryBuilderFactory qbf;

	@Override
	public String findByConcreteMethod(String anyValue) {
		return CONCRETE_METHOD_RETURN_VALUE;
	}

	/**
	 * this is not abstract and not be declared in the implements interface
	 *
	 * @return empty list
	 */
	public List<Person> findByNoSquery(Integer minAge) {
		IQueryBuilder<Person> qb = qbf.create(Person.class);
		IOperator where = qb.isGreaterThanOrEqualTo(qb.property(Person.AGE), qb.value(minAge));
		return qb.build(where).retrieve();
	}

	/**
	 * this is abstract and not be declared in the implements interface, this method have Squery
	 * feature
	 *
	 * @return not supply the return value
	 */
	public abstract List<Person> findByAgeLe(Integer maxAge);
}
