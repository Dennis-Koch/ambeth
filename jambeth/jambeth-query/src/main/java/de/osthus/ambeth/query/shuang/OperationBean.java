package de.osthus.ambeth.query.shuang;

import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQueryBuilder;

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

	public IOperand getOperand(IQueryBuilder<?> qb, Object value)
	{
		return condition.createOperand(qb, nestFieldName, value);
	}

	@Override
	public String toString()
	{
		return "OperationBean [fieldName=" + nestFieldName + ", condition=" + condition + ", relation=" + relation + "]";
	}

}
