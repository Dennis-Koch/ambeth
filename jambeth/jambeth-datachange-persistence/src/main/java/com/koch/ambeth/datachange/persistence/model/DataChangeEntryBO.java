package com.koch.ambeth.datachange.persistence.model;

/*-
 * #%L
 * jambeth-datachange-persistence
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

public class DataChangeEntryBO
{
	protected long id;

	protected byte version;

	protected EntityType entityType;

	protected byte idIndex;

	protected String objectId;

	protected String objectVersion;

	protected DataChangeEntryBO()
	{
		// Intended blank
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public byte getVersion()
	{
		return version;
	}

	public void setVersion(byte version)
	{
		this.version = version;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	public void setEntityType(EntityType entityType)
	{
		this.entityType = entityType;
	}

	public byte getIdIndex()
	{
		return idIndex;
	}

	public void setIdIndex(byte idIndex)
	{
		this.idIndex = idIndex;
	}

	public String getObjectId()
	{
		return objectId;
	}

	public void setObjectId(String objectId)
	{
		this.objectId = objectId;
	}

	public String getObjectVersion()
	{
		return objectVersion;
	}

	public void setObjectVersion(String objectVersion)
	{
		this.objectVersion = objectVersion;
	}
}
