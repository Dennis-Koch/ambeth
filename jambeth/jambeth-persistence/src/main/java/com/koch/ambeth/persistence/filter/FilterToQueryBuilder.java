package com.koch.ambeth.persistence.filter;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.criteria.JoinType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.koch.ambeth.filter.FilterOperator;
import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.filter.LogicalOperator;
import com.koch.ambeth.filter.SortDirection;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class FilterToQueryBuilder implements IFilterToQueryBuilder {
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	protected final Lock writeLock = new ReentrantLock();

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public <T> IPagingQuery<T> buildQuery(IFilterDescriptor<T> filterDescriptor,
			ISortDescriptor[] sortDescriptors) {
		Class<?> entityType = filterDescriptor.getEntityType();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		IValueObjectConfig valueObjectConfig = null;
		if (metaData == null) {
			valueObjectConfig = entityMetaDataProvider.getValueObjectConfig(entityType);
			if (valueObjectConfig != null) {
				entityType = valueObjectConfig.getEntityType();
			}
		}
		IQueryBuilder<?> queryBuilder = queryBuilderFactory.create(entityType);

		try {
			IOperand currentRootOperand =
					buildOperandFromFilterDescriptor(queryBuilder, filterDescriptor);

			if (sortDescriptors != null) {
				for (int a = 0, size = sortDescriptors.length; a < size; a++) {
					ISortDescriptor sortDescriptor = sortDescriptors[a];
					SortDirection sortDirection = sortDescriptor.getSortDirection();

					IOperand sortOperand = queryBuilder.property(sortDescriptor.getMember());
					switch (sortDirection) {
						case ASCENDING:
							queryBuilder.orderBy(sortOperand, OrderByType.ASC);
							break;
						case DESCENDING:
							queryBuilder.orderBy(sortOperand, OrderByType.DESC);
							break;
						default:
							throw new IllegalStateException(
									"Not supported " + SortDirection.class.getSimpleName() + ": " + sortDirection);
					}
				}
			}
			IPagingQuery<?> query = queryBuilder.buildPaging(currentRootOperand);
			queryBuilder.dispose();
			return (IPagingQuery<T>) query;
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected <T> IOperand buildOperandFromFilterDescriptor(IQueryBuilder<?> queryBuilder,
			IFilterDescriptor<T> filterDescriptor) {
		List<IFilterDescriptor<T>> childFilterDescriptors =
				filterDescriptor.getChildFilterDescriptors();

		if (childFilterDescriptors != null) {
			return buildOperandFromCompositeDescriptor(queryBuilder, filterDescriptor);
		}
		return buildOperandFromSimpleFilterDescriptor(queryBuilder, filterDescriptor);
	}

	protected <T> IOperand buildOperandFromCompositeDescriptor(IQueryBuilder<?> queryBuilder,
			IFilterDescriptor<T> filterDescriptor) {
		List<IFilterDescriptor<T>> childFilterDescriptors =
				filterDescriptor.getChildFilterDescriptors();

		LogicalOperator logicalOperator = filterDescriptor.getLogicalOperator();

		IOperand operand = buildBalancedTree(childFilterDescriptors, logicalOperator, queryBuilder);

		return operand;
	}

	protected <T> IOperand buildBalancedTree(List<IFilterDescriptor<T>> childFilterDescriptors,
			LogicalOperator logicalOperator, IQueryBuilder<?> queryBuilder) {
		IOperand operand = null;

		int childCount = childFilterDescriptors.size();

		if (childCount == 0) {
			throw new IllegalArgumentException(
					"CompositeFilterDescriptor with no ChildFilterDescriptors not allowed");
		}
		if (childCount == 1) {
			return buildOperandFromFilterDescriptor(queryBuilder, childFilterDescriptors.get(0));
		}

		IOperand operand1;
		IOperand operand2;
		if (childCount == 2) {
			operand1 = buildOperandFromFilterDescriptor(queryBuilder, childFilterDescriptors.get(0));
			operand2 = buildOperandFromFilterDescriptor(queryBuilder, childFilterDescriptors.get(1));
		}
		else {
			int splitIndex = childCount / 2;
			operand1 = buildBalancedTree(childFilterDescriptors.subList(0, splitIndex), logicalOperator,
					queryBuilder);
			operand2 = buildBalancedTree(childFilterDescriptors.subList(splitIndex, childCount),
					logicalOperator, queryBuilder);
		}

		switch (logicalOperator) {
			case AND:
				operand = queryBuilder.and(operand1, operand2);
				break;
			case OR:
				operand = queryBuilder.or(operand1, operand2);
				break;
			default:
				throw new IllegalStateException(
						"Not supported " + LogicalOperator.class.getSimpleName() + ": " + logicalOperator);
		}

		return operand;
	}

	protected <T> IOperand buildOperandFromSimpleFilterDescriptor(IQueryBuilder<?> queryBuilder,
			IFilterDescriptor<T> filterDescriptor) {
		IOperand operand = null;

		Boolean isCaseSensitive = filterDescriptor.isCaseSensitive();

		ParamHolder<Class<?>> columnType = new ParamHolder<>();

		String memberName = filterDescriptor.getMember();
		IOperand leftOperand = null;
		if (memberName != null) {
			leftOperand = queryBuilder.property(memberName, JoinType.LEFT, columnType);
		}
		FilterOperator filterOperator = filterDescriptor.getOperator();

		if (filterOperator == null) {
			if (memberName != null) {
				throw new IllegalArgumentException("FilterDescriptor with specific member '" + memberName
						+ "' has no valid " + FilterOperator.class.getName());
			}
			return queryBuilder.all();
		}
		List<String> values = filterDescriptor.getValue();
		String valueString = values.size() == 1 ? values.get(0) : null;

		Object value;
		switch (filterOperator) {
			case FULL_TEXT:
				if (valueString == null) {
					operand = queryBuilder.all();
				}
				operand = queryBuilder.fulltext(queryBuilder.value(valueString));
				break;
			case CONTAINS:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.contains(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case DOES_NOT_CONTAIN:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.notContains(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case ENDS_WITH:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.endsWith(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case IS_CONTAINED_IN:
				value = convertWithContext(columnType.getValue(), valueString);
				operand =
						queryBuilder.isContainedIn(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case IS_EQUAL_TO:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.isEqualTo(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case IS_GREATER_THAN:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.isGreaterThan(leftOperand, queryBuilder.value(value));
				break;
			case IS_GREATER_THAN_OR_EQUAL_TO:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.isGreaterThanOrEqualTo(leftOperand, queryBuilder.value(value));
				break;
			case IS_IN: {
				Object[] convertedValues = new Object[values.size()];
				for (int i = values.size(); i-- > 0;) {
					String item = values.get(i);
					convertedValues[i] = conversionHelper.convertValueToType(columnType.getValue(), item);
				}
				operand =
						queryBuilder.isIn(leftOperand, queryBuilder.value(convertedValues), isCaseSensitive);
				break;
			}
			case IS_LESS_THAN:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.isLessThan(leftOperand, queryBuilder.value(value));
				break;
			case IS_LESS_THAN_OR_EQUAL_TO:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.isLessThanOrEqualTo(leftOperand, queryBuilder.value(value));
				break;
			case IS_NOT_CONTAINED_IN:
				value = convertWithContext(columnType.getValue(), valueString);
				operand =
						queryBuilder.isNotContainedIn(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case IS_NOT_EQUAL_TO:
				value = convertWithContext(columnType.getValue(), valueString);
				operand =
						queryBuilder.isNotEqualTo(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case IS_NOT_IN: {
				Object[] convertedValues = new Object[values.size()];
				for (int i = values.size(); i-- > 0;) {
					String item = values.get(i);
					convertedValues[i] = conversionHelper.convertValueToType(columnType.getValue(), item);
				}
				operand =
						queryBuilder.isNotIn(leftOperand, queryBuilder.value(convertedValues), isCaseSensitive);
				break;
			}
			case LIKE:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.like(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case STARTS_WITH:
				value = convertWithContext(columnType.getValue(), valueString);
				operand = queryBuilder.startsWith(leftOperand, queryBuilder.value(value), isCaseSensitive);
				break;
			case IS_NOT_NULL:
				operand = queryBuilder.isNotNull(leftOperand);
				break;
			case IS_NULL:
				operand = queryBuilder.isNull(leftOperand);
				break;
			default:
				throw new IllegalStateException(
						"Not supported " + FilterOperator.class.getSimpleName() + ": " + filterOperator);
		}
		return operand;
	}

	/**
	 * We know the input comes from a SOAP format, so we know some sub-formats like date-time-strings.
	 *
	 * @param columnType
	 * @param valueString
	 * @return
	 */
	protected Object convertWithContext(Class<?> columnType, String valueString) {
		Object value = null;

		if (Timestamp.class.equals(columnType)) {
			value = conversionHelper.convertValueToType(XMLGregorianCalendar.class, valueString);
			value = conversionHelper.convertValueToType(columnType, value);
		}
		else {
			value = conversionHelper.convertValueToType(columnType, valueString);
		}

		return value;
	}
}
