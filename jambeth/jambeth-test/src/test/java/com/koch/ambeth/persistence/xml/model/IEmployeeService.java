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

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.util.annotation.Remove;

public interface IEmployeeService {
	List<Employee> getAll();

	Employee getByName(String name);

	@Cached(type = Employee.class, alternateIdName = "Name")
	List<Employee> retrieve(List<String> names);

	void save(Employee employee);

	void save(List<Employee> employees);

	void delete(Employee employee);

	void delete(List<Employee> employee);

	void delete(Set<Employee> employees);

	void delete(Employee[] employees);

	@Remove(entityType = Employee.class, idName = "Name")
	void delete(String name);

	List<Employee> retrieveOrderedByName(boolean reverse);

	Boat saveBoat(Boat boat);
}
