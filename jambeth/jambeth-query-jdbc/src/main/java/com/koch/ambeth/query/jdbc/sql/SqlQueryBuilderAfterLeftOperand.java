package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQueryBuilderAfterLeftOperand;
import com.koch.ambeth.query.jdbc.BasicTwoPlaceOperator;

public class SqlQueryBuilderAfterLeftOperand implements IQueryBuilderAfterLeftOperand {
	private final IOperand leftOperand;
	private final SqlQueryBuilder<?> queryBuilder;

	public SqlQueryBuilderAfterLeftOperand(SqlQueryBuilder<?> queryBuilder, IOperand leftOperand) {
		this.queryBuilder = queryBuilder;
		this.leftOperand = leftOperand;
	}

	@Override
	public IOperator contains(Object rightOperand) {
		return contains(rightOperand, null);
	}

	@Override
	public IOperator contains(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlContainsOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator endsWith(Object rightOperand) {
		return endsWith(rightOperand, null);
	}

	@Override
	public IOperator endsWith(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlEndsWithOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isContainedIn(Object rightOperand) {
		return isContainedIn(rightOperand, null);
	}

	@Override
	public IOperator isContainedIn(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlContainsOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isIn(Object rightOperand) {
		return isIn(rightOperand, null);
	}

	@Override
	public IOperator isIn(Object rightOperand, Boolean caseSensitive) {
		if (!(rightOperand instanceof IMultiValueOperand)
				&& !(rightOperand instanceof SqlSubselectOperand)) {
			throw new IllegalArgumentException("rightOperand must be an instance of "
					+ IMultiValueOperand.class.getName() + " or a sub-query");
		}
		return operand(SqlIsInOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isEqualTo(Object rightOperand) {
		return isEqualTo(rightOperand, null);
	}

	@Override
	public IOperator isEqualTo(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlIsEqualToOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isGreaterThan(Object rightOperand) {
		return operand(SqlIsGreaterThanOperator.class, rightOperand, null);
	}

	@Override
	public IOperator isGreaterThanOrEqualTo(Object rightOperand) {
		return operand(SqlIsGreaterThanOrEqualToOperator.class, rightOperand, null);
	}

	@Override
	public IOperator isLessThan(Object rightOperand) {
		return operand(SqlIsLessThanOperator.class, rightOperand, null);
	}

	@Override
	public IOperator isLessThanOrEqualTo(Object rightOperand) {
		return operand(SqlIsLessThanOrEqualToOperator.class, rightOperand, null);
	}

	@Override
	public IOperator isNotContainedIn(Object rightOperand) {
		return isNotContainedIn(rightOperand, null);
	}

	@Override
	public IOperator isNotContainedIn(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlNotContainsOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isNotIn(Object rightOperand) {
		return isNotIn(rightOperand, null);
	}

	@Override
	public IOperator isNotIn(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlIsNotInOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isNotEqualTo(Object rightOperand) {
		return isNotEqualTo(rightOperand, null);
	}

	@Override
	public IOperator isNotEqualTo(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlIsNotEqualToOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator notContains(Object rightOperand) {
		return notContains(rightOperand, null);
	}

	@Override
	public IOperator notContains(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlNotContainsOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator notLike(Object rightOperand) {
		return notLike(rightOperand, null);
	}

	@Override
	public IOperator notLike(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlNotLikeOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isNull() {
		return queryBuilder.isNull(leftOperand);
	}

	@Override
	public IOperator isNotNull() {
		return queryBuilder.isNotNull(leftOperand);
	}

	@Override
	public IOperator like(Object rightOperand) {
		return like(rightOperand, null);
	}

	@Override
	public IOperator like(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlLikeOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperand regexpLike(IOperand pattern) {
		return queryBuilder.regexpLike(leftOperand, pattern);
	}

	@Override
	public IOperand regexpLike(IOperand pattern, IOperand matchParameter) {
		return queryBuilder.regexpLike(leftOperand, pattern, matchParameter);
	}

	@Override
	public IOperator startsWith(Object rightOperand) {
		return startsWith(rightOperand, null);
	}

	@Override
	public IOperator startsWith(Object rightOperand, Boolean caseSensitive) {
		return operand(SqlStartsWithOperator.class, rightOperand, caseSensitive);
	}

	@Override
	public IOperand interval(Object rightOperand) {
		return queryBuilder.interval(leftOperand, queryBuilder.toOperand(rightOperand));
	}

	@Override
	public IOperand overlaps(Object rightOperand) {
		return queryBuilder.overlaps(leftOperand, queryBuilder.toOperand(rightOperand));
	}

	protected IOperator operand(Class<? extends BasicTwoPlaceOperator> operatorType,
			Object rightOperand, Boolean caseSensitive) {
		return queryBuilder.createTwoPlaceOperator(operatorType, leftOperand,
				queryBuilder.toOperand(rightOperand),
				caseSensitive);
	}
}
