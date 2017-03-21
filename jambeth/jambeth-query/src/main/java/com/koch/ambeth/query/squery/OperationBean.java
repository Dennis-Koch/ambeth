package com.koch.ambeth.query.squery;

/*-
 * #%L
 * jambeth-query
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

import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQueryBuilder;

public class OperationBean
{
	private String nestFieldName;
	private Condition condition;
	private Relation relation;

	public OperationBean(String nestFieldName, Condition condition, String relation)
	{
		this.nestFieldName = nestFieldName;
		this.condition = condition;
		this.relation = Relation.build(relation);
	}

	public String getNestFieldName()
	{
		return nestFieldName;
	}

	public Condition getCondition()
	{
		return condition;
	}

	public Relation getRelation()
	{
		return relation;
	}

	public IOperand buildOperand(IQueryBuilder<?> qb, Object value)
	{
		return condition.createOperand(qb, nestFieldName, value);
	}

	@Override
	public String toString()
	{
		return "OperationBean [fieldName=" + nestFieldName + ", condition=" + condition + ", relation=" + relation + "]";
	}

}
