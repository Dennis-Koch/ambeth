package com.koch.ambeth.query;

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

import com.koch.ambeth.model.AbstractEntity;
import com.koch.ambeth.util.annotation.Cascade;
import com.koch.ambeth.util.annotation.CascadeLoadMode;

public class QueryEntity extends AbstractEntity
{
	public static final String Name1 = "Name1";

	@Cascade(load = CascadeLoadMode.EAGER_VERSION)
	protected JoinQueryEntity fk;

	protected LinkTableEntity linkTableEntity;

	protected String name1;

	protected String name2;

	protected String ftname1;

	protected String ftname2;

	protected double content;

	protected QueryEntity()
	{
		// Intended blank
	}

	public JoinQueryEntity getFk()
	{
		return fk;
	}

	public void setFk(JoinQueryEntity fk)
	{
		this.fk = fk;
	}

	public LinkTableEntity getLinkTableEntity()
	{
		return linkTableEntity;
	}

	public void setLinkTableEntity(LinkTableEntity linkTableEntity)
	{
		this.linkTableEntity = linkTableEntity;
	}

	public String getName1()
	{
		return name1;
	}

	public void setName1(String name1)
	{
		this.name1 = name1;
	}

	public String getName2()
	{
		return name2;
	}

	public void setName2(String name2)
	{
		this.name2 = name2;
	}

	public void setFtname1(String ftname1)
	{
		this.ftname1 = ftname1;
	}

	public String getFtname1()
	{
		return ftname1;
	}

	public void setFtname2(String ftname2)
	{
		this.ftname2 = ftname2;
	}

	public String getFtname2()
	{
		return ftname2;
	}

	public double getContent()
	{
		return content;
	}

	public void setContent(double content)
	{
		this.content = content;
	}
}
