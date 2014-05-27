package de.osthus.ambeth.persistence.xml.model;

import de.osthus.ambeth.model.AbstractEntity;

public class Address extends AbstractEntity
{
	protected String street;

	protected String city;

	protected Employee resident;

	protected Address()
	{
		// Intended blank
	}

	public String getStreet()
	{
		return street;
	}

	public void setStreet(String street)
	{
		this.street = street;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public Employee getResident()
	{
		return resident;
	}

	public void setResident(Employee resident)
	{
		this.resident = resident;
	}
}
