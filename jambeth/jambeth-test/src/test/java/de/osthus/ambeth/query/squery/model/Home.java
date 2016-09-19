package de.osthus.ambeth.query.squery.model;

public class Home extends BaseEntity
{
	protected Address address;

	public Address getAddress()
	{
		return address;
	}

	public void setAddress(Address address)
	{
		this.address = address;
	}
}
