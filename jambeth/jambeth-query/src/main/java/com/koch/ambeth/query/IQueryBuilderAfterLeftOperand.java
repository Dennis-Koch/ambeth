package com.koch.ambeth.query;

public interface IQueryBuilderAfterLeftOperand {
	IOperator contains(Object rightOperand);

	IOperator contains(Object rightOperand, Boolean caseSensitive);

	IOperator endsWith(Object rightOperand);

	IOperator endsWith(Object rightOperand, Boolean caseSensitive);

	IOperator isContainedIn(Object rightOperand);

	IOperator isContainedIn(Object rightOperand, Boolean caseSensitive);

	IOperator isIn(Object rightOperand);

	IOperator isIn(Object rightOperand, Boolean caseSensitive);

	IOperator isEqualTo(Object rightOperand);

	IOperator isEqualTo(Object rightOperand, Boolean caseSensitive);

	IOperator isGreaterThan(Object rightOperand);

	IOperator isGreaterThanOrEqualTo(Object rightOperand);

	IOperator isLessThan(Object rightOperand);

	IOperator isLessThanOrEqualTo(Object rightOperand);

	IOperator isNotContainedIn(Object rightOperand);

	IOperator isNotContainedIn(Object rightOperand, Boolean caseSensitive);

	IOperator isNotIn(Object rightOperand);

	IOperator isNotIn(Object rightOperand, Boolean caseSensitive);

	IOperator isNotEqualTo(Object rightOperand);

	IOperator isNotEqualTo(Object rightOperand, Boolean caseSensitive);

	IOperator notContains(Object rightOperand);

	IOperator notContains(Object rightOperand, Boolean caseSensitive);

	IOperator notLike(Object rightOperand);

	IOperator notLike(Object rightOperand, Boolean caseSensitive);

	IOperator isNull();

	IOperator isNotNull();

	IOperator like(Object rightOperand);

	IOperator like(Object rightOperand, Boolean caseSensitive);

	IOperand regexpLike(IOperand pattern);

	IOperand regexpLike(IOperand pattern, IOperand matchParameter);

	IOperator startsWith(Object rightOperand);

	IOperator startsWith(Object rightOperand, Boolean caseSensitive);

	IOperand interval(Object rightOperand);

	IOperand overlaps(Object rightOperand);
}
