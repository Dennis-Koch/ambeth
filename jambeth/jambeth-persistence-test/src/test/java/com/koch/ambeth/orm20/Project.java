package com.koch.ambeth.orm20;

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

import java.util.HashSet;
import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;

public class Project extends AbstractEntity {
	protected String name;

	protected Set<Employee> employees;

	protected Project() {
		// Intended blank
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Set<Employee> getEmployees() {
		if (employees == null) {
			employees = new HashSet<>();
		}
		return employees;
	}

	public void setEmployees(Set<Employee> employees) {
		getEmployees().addAll(employees);
	}

	public boolean addEmployee(Employee employee) {
		return getEmployees().add(employee);
	}

	public boolean removeEmployee(Employee employee) {
		return getEmployees().remove(employee);
	}
}
