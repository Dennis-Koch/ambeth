package de.osthus.ambeth.query.squery.model;

import java.util.Date;

public class Person extends BaseEntity
{
	protected String name;
	protected Integer age;
	protected Home home;
	protected HomeAddress homeAddress;
	protected Boolean haveAndriod; // field name have And no problem
	protected Boolean haveOrange; // field name have Or no problem
	protected Date modifyTime;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getAge()
	{
		return age;
	}

	public void setAge(Integer age)
	{
		this.age = age;
	}

	public Home getHome()
	{
		return home;
	}

	public void setHome(Home home)
	{
		this.home = home;
	}

	public HomeAddress getHomeAddress()
	{
		return homeAddress;
	}

	public void setHomeAddress(HomeAddress homeAddress)
	{
		this.homeAddress = homeAddress;
	}

	public Boolean getHaveAndriod()
	{
		return haveAndriod;
	}

	public void setHaveAndriod(Boolean haveAndriod)
	{
		this.haveAndriod = haveAndriod;
	}

	public Boolean getHaveOrange()
	{
		return haveOrange;
	}

	public void setHaveOrange(Boolean haveOrange)
	{
		this.haveOrange = haveOrange;
	}

	public Date getModifyTime()
	{
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime)
	{
		this.modifyTime = modifyTime;
	}

}
