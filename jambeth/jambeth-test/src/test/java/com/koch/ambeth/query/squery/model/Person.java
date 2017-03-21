package com.koch.ambeth.query.squery.model;

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

import java.util.Date;

public class Person extends BaseEntity
{
	public static final String AGE = "Age";

	protected String name;
	protected Integer age;
	protected Home home; // have nest field: Home.Address.Name
	protected HomeAddress homeAddress; // have nest field: HomeAdress.Streat ***** these look like two fields no problem in squery
	protected Boolean haveAndriod; // field name have "And", this no problem
	protected Boolean haveOrange; // field name have "Or", this no problem
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
