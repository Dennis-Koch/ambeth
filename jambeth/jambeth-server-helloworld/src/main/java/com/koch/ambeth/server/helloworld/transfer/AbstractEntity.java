package com.koch.ambeth.server.helloworld.transfer;

/*-
 * #%L
 * jambeth-server-helloworld
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "HelloWorld")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEntity
{
	@XmlElement
	protected long id;

	@XmlElement
	protected int version;

	@XmlElement
	protected String updatedBy, createdBy;

	@XmlElement
	protected long updatedOn, createdOn;

	public void setId(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public int getVersion()
	{
		return version;
	}

	public String getUpdatedBy()
	{
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy)
	{
		this.updatedBy = updatedBy;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}

	public long getUpdatedOn()
	{
		return updatedOn;
	}

	public void setUpdatedOn(long updatedOn)
	{
		this.updatedOn = updatedOn;
	}

	public long getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(long createdOn)
	{
		this.createdOn = createdOn;
	}

}
