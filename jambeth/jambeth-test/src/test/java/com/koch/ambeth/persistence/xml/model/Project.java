package com.koch.ambeth.persistence.xml.model;

import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;

public abstract class Project extends AbstractEntity {
	public static final String Name = "Name";

	protected String name;

	protected Project() {
		// Intended blank
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract Set<Employee> getEmployees();

	public boolean addEmployee(Employee employee) {
		return getEmployees().add(employee);
	}

	public boolean removeEmployee(Employee employee) {
		return getEmployees().remove(employee);
	}
}
