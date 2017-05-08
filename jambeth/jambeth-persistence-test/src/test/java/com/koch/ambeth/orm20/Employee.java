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

import java.util.List;
import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;

public class Employee extends AbstractEntity {
	protected String name;

	protected List<String> nicknames;

	protected Address primaryAddress;

	protected Set<Address> otherAddresses;

	protected Employee supervisor;

	protected Project primaryProject;

	protected Project secondaryProject;

	protected Set<Project> allProjects;

	protected Car car;

	protected Boat boat;

	protected Employee() {
		// Intended blank
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getNicknames() {
		return nicknames;
	}

	public void setNicknames(List<String> nicknames) {
		this.nicknames = nicknames;
	}

	public Address getPrimaryAddress() {
		return primaryAddress;
	}

	public void setPrimaryAddress(Address primaryAddress) {
		this.primaryAddress = primaryAddress;
	}

	public Set<Address> getOtherAddresses() {
		return otherAddresses;
	}

	public void setOtherAddresses(Set<Address> otherAddresses) {
		this.otherAddresses = otherAddresses;
	}

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

	public Set<Project> getAllProjects() {
		return allProjects;
	}

	public void setAllProjects(Set<Project> projects) {
		allProjects = projects;
	}

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
}
