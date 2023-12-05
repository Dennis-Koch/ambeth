package com.koch.ambeth.persistence.filter;

/*-
 * #%L
 * jambeth-persistence
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
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamHolder;
import jakarta.persistence.criteria.JoinType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class FilterToQueryBuilder implements IFilterToQueryBuilder {
    protected final Lock writeLock = new ReentrantLock();
    @Autowired
    protected IServiceContext beanContext;

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IQueryBuilderFactory queryBuilderFactory;
    @LogInstance
    private ILogger log;

    @SuppressWarnings("unchecked")
    @Override
    @PersistenceContext(PersistenceContextType.REQUIRED)
    public <T> IPagingQuery<T> buildQuery(IFilterDescriptor<T> filterDescriptor, ISortDescriptor[] sortDescriptors) {
        Class<?> entityType = filterDescriptor.getEntityType();
        var metaData = entityMetaDataProvider.getMetaData(entityType, true);
        IValueObjectConfig valueObjectConfig = null;
        if (metaData == null) {
            valueObjectConfig = entityMetaDataProvider.getValueObjectConfig(entityType);
            if (valueObjectConfig != null) {
                entityType = valueObjectConfig.getEntityType();
            }
        }
        var queryBuilder = queryBuilderFactory.create(entityType);

        var currentRootOperand = buildOperandFromFilterDescriptor(queryBuilder, filterDescriptor);

        if (sortDescriptors != null) {
            for (int a = 0, size = sortDescriptors.length; a < size; a++) {
                var sortDescriptor = sortDescriptors[a];
                var sortDirection = sortDescriptor.getSortDirection();

                var sortOperand = queryBuilder.property(sortDescriptor.getMember());
                switch (sortDirection) {
                    case ASCENDING:
                        queryBuilder.orderBy(sortOperand, OrderByType.ASC);
                        break;
                    case DESCENDING:
                        queryBuilder.orderBy(sortOperand, OrderByType.DESC);
                        break;
                    default:
                        throw new IllegalStateException("Not supported " + SortDirection.class.getSimpleName() + ": " + sortDirection);
                }
            }
        }
        var query = queryBuilder.buildPaging(currentRootOperand);
        queryBuilder.dispose();
        return (IPagingQuery<T>) query;
    }

    protected <T> IOperand buildOperandFromFilterDescriptor(IQueryBuilder<?> queryBuilder, IFilterDescriptor<T> filterDescriptor) {
        var childFilterDescriptors = filterDescriptor.getChildFilterDescriptors();

        if (childFilterDescriptors != null) {
            return buildOperandFromCompositeDescriptor(queryBuilder, filterDescriptor);
        }
        return buildOperandFromSimpleFilterDescriptor(queryBuilder, filterDescriptor);
    }

    protected <T> IOperand buildOperandFromCompositeDescriptor(IQueryBuilder<?> queryBuilder, IFilterDescriptor<T> filterDescriptor) {
        var childFilterDescriptors = filterDescriptor.getChildFilterDescriptors();

        var logicalOperator = filterDescriptor.getLogicalOperator();

        var operand = buildBalancedTree(childFilterDescriptors, logicalOperator, queryBuilder);

        return operand;
    }

    protected <T> IOperand buildBalancedTree(List<IFilterDescriptor<T>> childFilterDescriptors, LogicalOperator logicalOperator, IQueryBuilder<?> queryBuilder) {
        IOperand operand = null;

        var childCount = childFilterDescriptors.size();

        if (childCount == 0) {
            throw new IllegalArgumentException("CompositeFilterDescriptor with no ChildFilterDescriptors not allowed");
        }
        if (childCount == 1) {
            return buildOperandFromFilterDescriptor(queryBuilder, childFilterDescriptors.get(0));
        }

        IOperand operand1;
        IOperand operand2;
        if (childCount == 2) {
            operand1 = buildOperandFromFilterDescriptor(queryBuilder, childFilterDescriptors.get(0));
            operand2 = buildOperandFromFilterDescriptor(queryBuilder, childFilterDescriptors.get(1));
        } else {
            int splitIndex = childCount / 2;
            operand1 = buildBalancedTree(childFilterDescriptors.subList(0, splitIndex), logicalOperator, queryBuilder);
            operand2 = buildBalancedTree(childFilterDescriptors.subList(splitIndex, childCount), logicalOperator, queryBuilder);
        }

        switch (logicalOperator) {
            case AND:
                operand = queryBuilder.and(operand1, operand2);
                break;
            case OR:
                operand = queryBuilder.or(operand1, operand2);
                break;
            default:
                throw new IllegalStateException("Not supported " + LogicalOperator.class.getSimpleName() + ": " + logicalOperator);
        }

        return operand;
    }

    protected <T> IOperand buildOperandFromSimpleFilterDescriptor(IQueryBuilder<?> queryBuilder, IFilterDescriptor<T> filterDescriptor) {
        IOperand operand = null;

        var isCaseSensitive = filterDescriptor.isCaseSensitive();

        var columnType = new ParamHolder<Class<?>>();

        var memberName = filterDescriptor.getMember();
        IOperand leftOperand = null;
        if (memberName != null) {
            leftOperand = queryBuilder.property(memberName, JoinType.LEFT, columnType);
        }
        var filterOperator = filterDescriptor.getOperator();

        if (filterOperator == null) {
            if (memberName != null) {
                throw new IllegalArgumentException("FilterDescriptor with specific member '" + memberName + "' has no valid " + FilterOperator.class.getName());
            }
            return queryBuilder.all();
        }
        var values = filterDescriptor.getValue();
        var valueString = values.size() == 1 ? values.get(0) : null;

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
                operand = queryBuilder.let(leftOperand).contains(queryBuilder.value(value), isCaseSensitive);
                break;
            case DOES_NOT_CONTAIN:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).notContains(queryBuilder.value(value), isCaseSensitive);
                break;
            case ENDS_WITH:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).endsWith(queryBuilder.value(value), isCaseSensitive);
                break;
            case IS_CONTAINED_IN:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).isContainedIn(queryBuilder.value(value), isCaseSensitive);
                break;
            case IS_EQUAL_TO:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).isEqualTo(queryBuilder.value(value), isCaseSensitive);
                break;
            case IS_GREATER_THAN:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).isGreaterThan(queryBuilder.value(value));
                break;
            case IS_GREATER_THAN_OR_EQUAL_TO:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).isGreaterThanOrEqualTo(queryBuilder.value(value));
                break;
            case IS_IN: {
                var convertedValues = new Object[values.size()];
                for (int i = values.size(); i-- > 0; ) {
                    var item = values.get(i);
                    convertedValues[i] = conversionHelper.convertValueToType(columnType.getValue(), item);
                }
                operand = queryBuilder.let(leftOperand).isIn(queryBuilder.value(convertedValues), isCaseSensitive);
                break;
            }
            case IS_LESS_THAN:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).isLessThan(queryBuilder.value(value));
                break;
            case IS_LESS_THAN_OR_EQUAL_TO:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).isLessThanOrEqualTo(queryBuilder.value(value));
                break;
            case IS_NOT_CONTAINED_IN:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).isNotContainedIn(queryBuilder.value(value), isCaseSensitive);
                break;
            case IS_NOT_EQUAL_TO:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).isNotEqualTo(queryBuilder.value(value), isCaseSensitive);
                break;
            case IS_NOT_IN: {
                var convertedValues = new Object[values.size()];
                for (int i = values.size(); i-- > 0; ) {
                    var item = values.get(i);
                    convertedValues[i] = conversionHelper.convertValueToType(columnType.getValue(), item);
                }
                operand = queryBuilder.let(leftOperand).isNotIn(queryBuilder.value(convertedValues), isCaseSensitive);
                break;
            }
            case LIKE:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).like(queryBuilder.value(value), isCaseSensitive);
                break;
            case STARTS_WITH:
                value = convertWithContext(columnType.getValue(), valueString);
                operand = queryBuilder.let(leftOperand).startsWith(queryBuilder.value(value), isCaseSensitive);
                break;
            case IS_NOT_NULL:
                operand = queryBuilder.let(leftOperand).isNotNull();
                break;
            case IS_NULL:
                operand = queryBuilder.let(leftOperand).isNull();
                break;
            default:
                throw new IllegalStateException("Not supported " + FilterOperator.class.getSimpleName() + ": " + filterOperator);
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
        } else {
            value = conversionHelper.convertValueToType(columnType, valueString);
        }

        return value;
    }
}
