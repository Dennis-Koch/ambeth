package com.koch.ambeth.filter;

/*-
 * #%L
 * jambeth-filter
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
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class SortDescriptor implements ISortDescriptor
{
	@XmlElement(name = "Member", required = true)
	protected String member;

	@XmlElement(name = "SortDirection", required = true)
	protected SortDirection sortDirection = SortDirection.ASCENDING;

	@Override
	public String getMember()
	{
		return member;
	}

	public void setMember(String member)
	{
		this.member = member;
	}

	public SortDescriptor withMember(String member)
	{
		setMember(member);
		return this;
	}

	@Override
	public SortDirection getSortDirection()
	{
		return sortDirection;
	}

	public void setSortDirection(SortDirection sortDirection)
	{
		this.sortDirection = sortDirection;
	}

	public SortDescriptor withSortDirection(SortDirection sortDirection)
	{
		setSortDirection(sortDirection);
		return this;
	}
}
