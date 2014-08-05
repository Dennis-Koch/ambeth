package de.osthus.ambeth.orm20;

import java.util.HashSet;
import java.util.Set;

import de.osthus.ambeth.model.AbstractEntity;

public class Project extends AbstractEntity
{
	protected String name;

	protected Set<Employee> employees;

	protected Project()
	{
		// Intended blank
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public Set<Employee> getEmployees()
	{
		if (employees == null)
		{
			employees = new HashSet<Employee>();
		}
		return employees;
	}

	public void setEmployees(Set<Employee> employees)
	{
		getEmployees().addAll(employees);
	}

	public boolean addEmployee(Employee employee)
	{
		return getEmployees().add(employee);
	}

	public boolean removeEmployee(Employee employee)
	{
		return getEmployees().remove(employee);
	}
}
