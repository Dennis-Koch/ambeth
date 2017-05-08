package com.koch.ambeth.model;

/*-
 * #%L
 * jambeth-persistence-test
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

import com.koch.ambeth.util.annotation.EntityEqualsAspect;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
@EntityEqualsAspect
public class Material {
	protected int id;

	protected short version;

	protected String name;

	protected String buid;

	protected Unit unit;

	protected MaterialGroup materialGroup;

	protected Date createdOn;

	protected String createdBy;

	protected Date updatedOn;

	protected String updatedBy;

	protected Material() {
		// Intended blank
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public short getVersion() {
		return version;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setBuid(String buid) {
		this.buid = buid;
	}

	public String getBuid() {
		return buid;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setMaterialGroup(MaterialGroup materialGroup) {
		this.materialGroup = materialGroup;
	}

	public MaterialGroup getMaterialGroup() {
		return materialGroup;
	}
}
