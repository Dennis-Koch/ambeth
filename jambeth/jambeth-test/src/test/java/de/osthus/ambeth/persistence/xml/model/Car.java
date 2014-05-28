package de.osthus.ambeth.persistence.xml.model;

import javax.persistence.Embeddable;

@Embeddable
public class Car
{
	protected String make;

	protected String model;

	public String getMake()
	{
		return make;
	}

	public void setMake(String make)
	{
		this.make = make;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel(String model)
	{
		this.model = model;
	}
}
