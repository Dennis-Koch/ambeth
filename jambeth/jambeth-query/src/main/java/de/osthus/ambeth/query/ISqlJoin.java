package de.osthus.ambeth.query;

public interface ISqlJoin extends IOperator
{
	IOperand getJoinedColumn();

	String getTableName();

	String getFullqualifiedEscapedTableName();
}
