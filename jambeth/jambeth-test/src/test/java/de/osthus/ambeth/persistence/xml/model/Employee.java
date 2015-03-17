package de.osthus.ambeth.persistence.xml.model;

import java.util.List;
import java.util.Set;

import de.osthus.ambeth.model.AbstractEntity;

public abstract class Employee extends AbstractEntity
{
	public static final String Name = "Name";

	protected String name;

	protected Address primaryAddress;

	protected Employee supervisor;

	protected Project primaryProject;

	protected Project secondaryProject;

	protected Car car;

	protected Boat boat;

	protected Employee()
	{
		// Intended blank
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public abstract List<String> getNicknames();

	public Address getPrimaryAddress()
	{
		return primaryAddress;
	}

	public void setPrimaryAddress(Address primaryAddress)
	{
		this.primaryAddress = primaryAddress;
	}

	public abstract Set<Address> getOtherAddresses();

	public Employee getSupervisor()
	{
		return supervisor;
	}

	public void setSupervisor(Employee supervisor)
	{
		this.supervisor = supervisor;
	}

	public Project getPrimaryProject()
	{
		return primaryProject;
	}

	public void setPrimaryProject(Project primaryProject)
	{
		this.primaryProject = primaryProject;
	}

	public Project getSecondaryProject()
	{
		return secondaryProject;
	}

	public void setSecondaryProject(Project secondaryProject)
	{
		this.secondaryProject = secondaryProject;
	}

	public abstract Set<Project> getAllProjects();

	public Car getCar()
	{
		return car;
	}

	public void setCar(Car car)
	{
		this.car = car;
	}

	public Boat getBoat()
	{
		return boat;
	}

	public void setBoat(Boat boat)
	{
		this.boat = boat;
	}
}
