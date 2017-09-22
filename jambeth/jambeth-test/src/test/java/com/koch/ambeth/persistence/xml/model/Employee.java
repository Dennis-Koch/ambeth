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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;

public abstract class Employee extends AbstractEntity {
	public static final String Name = "Name";

	protected Optional<String> name2;

	protected String name;

	protected Address primaryAddress;

	protected Employee supervisor;

	protected Project primaryProject;

	protected Project secondaryProject;

	protected Car car;

	protected Boat boat;

	protected Map<Object, Object> attributes;

	protected Employee() {
		// Intended blank
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Optional<String> getName2() {
		return name2;
	}

	public void setName2(Optional<String> name2) {
		this.name2 = name2;
	}

	public abstract List<String> getNicknames();

	public Address getPrimaryAddress() {
		return primaryAddress;
	}

	public void setPrimaryAddress(Address primaryAddress) {
		this.primaryAddress = primaryAddress;
	}

	public abstract Set<Address> getOtherAddresses();

	public Employee getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(Employee supervisor) {
		this.supervisor = supervisor;
	}

	public Project getPrimaryProject() {
		return primaryProject;
	}

	public void setPrimaryProject(Project primaryProject) {
		this.primaryProject = primaryProject;
	}

	public Project getSecondaryProject() {
		return secondaryProject;
	}

	public void setSecondaryProject(Project secondaryProject) {
		this.secondaryProject = secondaryProject;
	}

	public abstract Set<Project> getAllProjects();

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public Boat getBoat() {
		return boat;
	}

	public void setBoat(Boat boat) {
		this.boat = boat;
	}

	public abstract Map<Object, Object> getAttributes();

	public abstract void setAttributes(Map<Object, Object> attributes);
}
