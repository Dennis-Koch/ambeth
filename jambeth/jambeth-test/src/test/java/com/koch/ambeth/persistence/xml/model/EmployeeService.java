package com.koch.ambeth.persistence.xml.model;

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
import java.util.Set;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.service.proxy.Service;

@Service(IEmployeeService.class)
@PersistenceContext
public class EmployeeService implements IInitializingBean, IEmployeeService, IStartingBean {
	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQuery<Employee> queryEmployeeByName;

	protected IQuery<Employee> queryEmployeeAll;

	protected IQuery<Employee> queryEmployeeByNameOrderedAsc;

	protected IQuery<Employee> queryEmployeeByNameOrderedDesc;

	@Override
	public void afterPropertiesSet() throws Throwable {

	}

	@Override
	public void afterStarted() throws Throwable {
		{
			IQueryBuilder<Employee> qb = queryBuilderFactory.create(Employee.class);
			queryEmployeeByName = qb
					.build(qb.isEqualTo(qb.property(Employee.Name), qb.valueName(Employee.Name)));
		}
		{
			queryEmployeeAll = queryBuilderFactory.create(Employee.class).build();
		}
		{
			IQueryBuilder<Employee> qb = queryBuilderFactory.create(Employee.class);
			IOperand nameOp = qb.property(Employee.Name);
			qb.orderBy(nameOp, OrderByType.ASC);
			queryEmployeeByNameOrderedAsc = qb.build();
		}
		{
			IQueryBuilder<Employee> qb = queryBuilderFactory.create(Employee.class);
			IOperand nameOp = qb.property(Employee.Name);
			qb.orderBy(nameOp, OrderByType.DESC);
			queryEmployeeByNameOrderedDesc = qb.build();
		}
	}

	@Override
	public List<Employee> getAll() {
		return queryEmployeeAll.retrieve();
	}

	@Override
	public Employee getByName(String name) {
		return queryEmployeeByName.param(Employee.Name, name).retrieveSingle();
	}

	@Override
	public List<Employee> retrieve(List<String> names) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(Employee employee) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(List<Employee> employees) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Employee employee) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(List<Employee> employees) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Set<Employee> employees) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Employee[] employees) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Employee> retrieveOrderedByName(boolean reverse) {
		if (reverse) {
			return queryEmployeeByNameOrderedDesc.retrieve();
		}
		return queryEmployeeByNameOrderedAsc.retrieve();
	}

	@Override
	public Boat saveBoat(Boat boat) {
		throw new UnsupportedOperationException();
	}
}
