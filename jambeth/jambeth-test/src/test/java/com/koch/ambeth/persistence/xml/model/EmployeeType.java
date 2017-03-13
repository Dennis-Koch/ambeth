package com.koch.ambeth.persistence.xml.model;

import java.util.HashSet;
import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;

public class EmployeeType extends AbstractEntity
{
	protected String name;

	protected Address primaryAddress;

	protected Set<Address> addresses;

	protected EmployeeType supervisor;

	protected Project primaryProject;

	protected Project secondaryProject;

	protected Project[] projects;

	protected String carMake;

	protected String carModel;

	protected EmployeeType()
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

	public String getInitials()
	{
		String initials = "";
		String[] parts = name.split(" ");
		for (int i = 0; i < parts.length; i++)
		{
			initials += parts[i].charAt(0);
		}
		return initials;
	}

	public Address getPrimaryAddress()
	{
		return primaryAddress;
	}

	public void setPrimaryAddress(Address primaryAddress)
	{
		this.primaryAddress = primaryAddress;
	}

	public Set<Address> getOtherAddresses()
	{
		if (addresses == null)
		{
			addresses = new HashSet<Address>();
		}
		return addresses;
	}

	public void setOtherAddresses(Set<Address> additionalAddresses)
	{
		this.addresses = additionalAddresses;
	}

	public EmployeeType getSupervisor()
	{
		return supervisor;
	}

	public void setSupervisor(EmployeeType supervisor)
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

	public Project[] getAllProjects()
	{
		return projects;
	}

	public void setAllProjects(Project[] projects)
	{
		this.projects = projects;
	}

	public String getCarMake()
	{
		return carMake;
	}

	public void setCarMake(String carMake)
	{
		this.carMake = carMake;
	}

	public String getCarModel()
	{
		return carModel;
	}

	public void setCarModel(String carModel)
	{
		this.carModel = carModel;
	}
}
