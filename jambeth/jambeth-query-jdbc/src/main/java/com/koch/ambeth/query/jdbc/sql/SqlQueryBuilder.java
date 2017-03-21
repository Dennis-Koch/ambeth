package com.koch.ambeth.query.jdbc.sql;

/*-
 * #%L
 * jambeth-query-jdbc
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.criteria.JoinType;

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyFactory;
import com.koch.ambeth.ioc.proxy.Self;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.filter.PagingQuery;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderExtension;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.IQueryBuilderIntern;
import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.query.ISubQuery;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.query.QueryType;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.query.jdbc.BasicTwoPlaceOperator;
import com.koch.ambeth.query.jdbc.FindFirstValueOperand;
import com.koch.ambeth.query.jdbc.ISubQueryIntern;
import com.koch.ambeth.query.jdbc.Query;
import com.koch.ambeth.query.jdbc.QueryDelegate;
import com.koch.ambeth.query.jdbc.StringQuery;
import com.koch.ambeth.query.jdbc.SubQuery;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class SqlQueryBuilder<T> implements IInitializingBean, IQueryBuilderIntern<T> {
	@LogInstance
	private ILogger log;

	public static final ISqlJoin[] emptyJoins = new ISqlJoin[0];

	public static final IOperand[] emptyOperands = new IOperand[0];

	protected static final Pattern PATTERN_CONTAINS_JOIN = Pattern.compile("\\.");

	protected static final Pattern PATTERN_ALLOWED_SEPARATORS = Pattern.compile("[\\.\\s]+");

	protected static final Pattern PATTERN_ENTITY_NAME_WITH_MARKER =
			Pattern.compile("([^A-Z]*[A-Z][^\\.]*)#");

	@Autowired
	protected IClassLoaderProvider classLoaderProvider;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IGarbageProxyFactory garbageProxyFactory;

	@Autowired
	protected ILoggerHistory loggerHistory;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired
	protected ITableAliasProvider tableAliasProvider;

	@Autowired
	protected ITransaction transaction;

	@Self
	protected IQueryBuilderIntern<T> self;

	@Property(defaultValue = "true")
	protected boolean disposeContextOnDispose;

	@Property
	protected Class<?> entityType;

	@Property
	protected IQueryBuilderExtension[] queryBuilderExtensions;

	protected IList<IOperand> groupByOperands;

	protected IList<IOperand> orderByOperands;

	protected IOperand limitOperand;

	protected IList<IOperand> selectOperands;

	protected final LinkedHashMap<String, ISqlJoin> joinMap = new LinkedHashMap<>();

	protected final IList<SqlSubselectOperand> subQueries = new ArrayList<>();

	protected final ITableAliasHolder tableAliasHolder = new TableAliasHolder();

	protected final LinkedHashSet<Class<?>> relatedEntityTypes = new LinkedHashSet<>();

	protected boolean disposed = false;

	@Override
	public void afterPropertiesSet() throws Throwable {
		relatedEntityTypes.add(entityType);
		tableAliasHolder.setTableAlias(tableAliasProvider.getNextSubQueryAlias());
	}

	@Override
	public synchronized void dispose() {
		if (disposed) {
			return;
		}
		disposed = true;
		entityType = null;
		relatedEntityTypes.clear();
		joinMap.clear();
		groupByOperands = null;
		orderByOperands = null;
		limitOperand = null;
		selectOperands = null;
		if (disposeContextOnDispose) {
			beanContext.dispose();
		}
		beanContext = null;
	}

	@Override
	public Class<?> getEntityType() {
		return entityType;
	}

	protected IServiceContext getBeanContext() {
		if (disposed) {
			throw new IllegalStateException("This query builder already is finalized!");
		}
		return beanContext;
	}

	protected BasicTwoPlaceOperator createTwoPlaceOperator(
			Class<? extends BasicTwoPlaceOperator> operatorType, Object leftOperand, Object rightOperand,
			Boolean caseSensitive) {
		ParamChecker.assertParamNotNull(operatorType, "operatorType");
		ParamChecker.assertParamNotNull(leftOperand, "leftOperand");
		ParamChecker.assertParamNotNull(rightOperand, "rightOperand");
		try {
			IBeanRuntime<? extends BasicTwoPlaceOperator> operatorBC =
					beanContext.registerBean(operatorType).propertyValue("LeftOperand", leftOperand)
							.propertyValue("RightOperand", rightOperand);
			if (caseSensitive != null) {
				operatorBC.propertyValue("CaseSensitive", caseSensitive);
			}
			return operatorBC.finish();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperand difference(IOperand... diffOperands) {
		return beanContext.registerBean(DifferenceOperand.class).propertyValue("Operands", diffOperands)
				.finish();
	}

	@Override
	public IOperator and(IOperand leftOperand, IOperand rightOperand) {
		return createTwoPlaceOperator(SqlAndOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator and(IOperand... operands) {
		if (operands.length == 0) {
			return trueOperator();
		}
		if (operands.length == 1) {
			if (operands[0] instanceof IOperator) {
				return (IOperator) operands[0];
			}
			else {
				throw new IllegalArgumentException("Cannot build 'and' statement with just one operand");
			}
		}

		IOperator currentOperator = and(operands[0], operands[1]);
		for (int i = operands.length; i-- > 2;) {
			IOperand operand = operands[i];
			currentOperator = and(currentOperator, operand);
		}

		return currentOperator;
	}

	@Override
	public IOperator or(IOperand leftOperand, IOperand rightOperand) {
		return createTwoPlaceOperator(SqlOrOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator or(IOperand... operands) {
		if (operands.length == 0) {
			return falseOperator();
		}
		if (operands.length == 1) {
			if (operands[0] instanceof IOperator) {
				return (IOperator) operands[0];
			}
			else {
				throw new IllegalArgumentException("Cannot build 'or' statement with just one operand");
			}
		}

		IOperator currentOperator = or(operands[0], operands[1]);
		for (int i = operands.length; i-- > 2;) {
			IOperand operand = operands[i];
			currentOperator = or(currentOperator, operand);
		}

		return currentOperator;
	}

	@Override
	public IOperand timeUnitMultipliedInterval(IOperand timeUnit, IOperand multiplicatedInterval) {
		ParamChecker.assertParamNotNull(timeUnit, "timeUnit");
		ParamChecker.assertParamNotNull(multiplicatedInterval, "multiplicatedInterval");
		return beanContext.registerBean(TimeUnitMultipliedOperand.class)
				.propertyValue("TimeUnit", timeUnit)
				.propertyValue("MultiplicatedInterval", multiplicatedInterval).finish();
	}

	@Override
	public IOperator trueOperator() {
		return and(value(1), value(1));
	}

	@Override
	public IOperator falseOperator() {
		return and(value(1), value(2));
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperand property(String propertyName) {
		return property(propertyName, JoinType.LEFT);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperand property(String propertyName, JoinType joinType) {
		return property(propertyName, joinType, null);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperand property(String propertyName, JoinType joinType,
			IParamHolder<Class<?>> fieldType) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder joinName = tlObjectCollector.create(StringBuilder.class);
		StringBuilder remainingJoinNameSB = tlObjectCollector.create(StringBuilder.class);
		ArrayList<String> propertyByJoinHierarchyList = new ArrayList<>();
		ArrayList<Class<?>> stepViaEntity = new ArrayList<>();
		ArrayList<Boolean> stepReverse = new ArrayList<>();
		propertyName = PATTERN_ALLOWED_SEPARATORS.matcher(propertyName).replaceAll(".");
		try {
			Class<?> entityType = this.entityType;

			Class<?> currentEntityType = entityType;
			remainingJoinNameSB.append(propertyName);
			while (remainingJoinNameSB.length() > 0) {
				String remainingJoinName = remainingJoinNameSB.toString();
				remainingJoinNameSB.setLength(0);
				joinName.setLength(0);
				joinName.append(remainingJoinName);
				while (true) {
					String currentPropertyName = joinName.toString();
					IEntityMetaData metaData = entityMetaDataProvider.getMetaData(currentEntityType, true);
					if (metaData == null) {
						propertyByJoinHierarchyList.add(currentPropertyName);
						remainingJoinNameSB.setLength(0);
						break;
					}
					relatedEntityTypes.add(currentEntityType);
					Member member = metaData.getMemberByName(currentPropertyName);
					if (member != null) {
						stepViaEntity.add(currentEntityType);
						stepReverse.add(Boolean.FALSE);
						currentEntityType = member.getElementType();
						propertyByJoinHierarchyList.add(currentPropertyName);

						if (joinName.length() + 1 <= remainingJoinName.length()) {
							remainingJoinNameSB.append(remainingJoinName, joinName.length() + 1,
									remainingJoinName.length());
						}
						else {
							IEntityMetaData currentMetaData =
									entityMetaDataProvider.getMetaData(currentEntityType, true);
							if (currentMetaData != null) {
								propertyByJoinHierarchyList.add(currentMetaData.getIdMember().getName());
							}
						}
						break;
					}
					else if (currentPropertyName.startsWith("<")) {
						String backwardsPropertyName = currentPropertyName.substring(1);
						String targetEntityName = null;

						Class<?> targetEntityType = null;
						if (backwardsPropertyName.contains("#")) {
							Matcher matcher = PATTERN_ENTITY_NAME_WITH_MARKER.matcher(backwardsPropertyName);
							if (!matcher.find()) {
								throw new IllegalArgumentException(
										"Unreadable property join definition: " + propertyName);
							}
							targetEntityName = matcher.group(1);
							try {
								targetEntityType = classLoaderProvider.getClassLoader().loadClass(targetEntityName);
							}
							catch (ClassNotFoundException e) {
								// intended blank
							}
							if (targetEntityType != null) {
								targetEntityType =
										entityMetaDataProvider.getMetaData(targetEntityType).getEntityType();
							}
							backwardsPropertyName = matcher.replaceFirst("");
						}

						Class<?> nextEntityType = null;
						Class<?>[] typesRelatingToThis = metaData.getTypesRelatingToThis();
						for (Class<?> other : typesRelatingToThis) {
							if ((targetEntityType == null && targetEntityName != null
									&& !targetEntityName.equals(other.getSimpleName())
									&& !targetEntityName.equals(other.getName()))
									|| (targetEntityType != null && !targetEntityType.equals(other))) {
								continue;
							}
							IEntityMetaData metaData2 = entityMetaDataProvider.getMetaData(other);
							Member backwardsMember = metaData2.getMemberByName(backwardsPropertyName);
							if (backwardsMember != null) {
								if (nextEntityType != null) {
									throw new IllegalStateException(
											"Join via reverse Property not unique: " + propertyName);
								}
								member = backwardsMember;
								nextEntityType = metaData2.getEntityType();
								if (targetEntityName != null) {
									break;
								}
							}
						}
						if (member != null) {
							currentEntityType = nextEntityType;
							stepViaEntity.add(currentEntityType);
							stepReverse.add(Boolean.TRUE);
							propertyByJoinHierarchyList.add(backwardsPropertyName);

							if (joinName.length() + 1 <= remainingJoinName.length()) {
								remainingJoinNameSB.append(remainingJoinName, joinName.length() + 1,
										remainingJoinName.length());
							}
							else {
								IEntityMetaData currentMetaData =
										entityMetaDataProvider.getMetaData(currentEntityType);
								propertyByJoinHierarchyList.add(currentMetaData.getIdMember().getName());
							}
							break;
						}
					}
					int lastIndexOfDot = joinName.lastIndexOf(".");
					if (lastIndexOfDot == -1) {
						throw new IllegalArgumentException("No property with name '" + remainingJoinName
								+ "' found on entity '" + currentEntityType.getName() + "'");
					}
					joinName.setLength(lastIndexOfDot);
				}
			}
			joinName.setLength(0);
			ISqlJoin join = null;

			int i = 0;
			for (; i < propertyByJoinHierarchyList.size() - 1; i++) {
				String joinPart = propertyByJoinHierarchyList.get(i);
				entityType = stepViaEntity.get(i);
				ITableMetaData table = databaseMetaData.getTableByType(entityType);
				if (joinName.length() > 0) {
					joinName.append('.');
				}
				joinName.append(joinPart);
				ISqlJoin prevJoin = join;

				IDirectedLinkMetaData dLink = table.getLinkByMemberName(joinPart);
				if (dLink == null) {
					throw new IllegalArgumentException("Property not mapped: " + joinPart);
				}
				Boolean reverse = stepReverse.get(i);
				if (reverse) {
					dLink = dLink.getReverseLink();
				}
				entityType = dLink.getToTable().getEntityType();

				String joinKey = joinName.toString();
				if (reverse) {
					joinKey = entityType.getName() + "#" + joinKey;
				}
				join = joinMap.get(joinKey);
				if (join != null) {
					continue;
				}
				IFieldMetaData currentFromField = dLink.getFromField();
				IFieldMetaData currentToField = dLink.getToField();

				if (dLink.getLink().hasLinkTable()) {
					String linkJoinKey = joinKey + ".link";
					Class<?> fromEntityType = dLink.getFromEntityType();
					join = joinMap.get(linkJoinKey);
					if (join == null) {
						byte fromMemberIdIndex = dLink.getFromField().getIdIndex();
						IEntityMetaData fromMetaData = entityMetaDataProvider.getMetaData(fromEntityType);
						String fromMemberName = fromMetaData.getIdMemberByIdIndex(fromMemberIdIndex).getName();
						IFieldMetaData fromField = dLink.getFromTable().getFieldByPropertyName(fromMemberName);
						IOperand columnBase = columnIntern(fromField.getName(), fromField, prevJoin);

						join = joinIntern(dLink.getLink().getName(), columnBase,
								columnIntern(currentFromField.getName(), currentFromField, null), joinType, null);

						joinMap.put(linkJoinKey, join);
					}
					prevJoin = join;
					IEntityMetaData toMetaData = entityMetaDataProvider.getMetaData(fromEntityType);
					byte toMemberIdIndex = dLink.getToField().getIdIndex();
					String toMemberName = toMetaData.getIdMemberByIdIndex(toMemberIdIndex).getName();
					IFieldMetaData toField = dLink.getToTable().getFieldByPropertyName(toMemberName);
					currentFromField = currentToField;
					currentToField = toField;
				}

				IOperand columnBase = columnIntern(currentFromField.getName(), currentFromField, prevJoin);
				IOperand columnTarget = columnIntern(currentToField.getName(), currentToField, null);
				join = join(entityType, columnBase, columnTarget, joinType);

				joinMap.put(joinKey, join);
			}

			ITableMetaData table = databaseMetaData.getTableByType(entityType);
			IFieldMetaData field = table.getFieldByPropertyName(propertyByJoinHierarchyList.get(i));
			if (field == null) {
				throw new IllegalArgumentException("Property not mapped: " + propertyName);
			}
			if (fieldType != null) {
				Class<?> currentFieldType = field.getFieldType();
				if (!java.sql.Array.class.isAssignableFrom(currentFieldType)) {
					fieldType.setValue(currentFieldType);
				}
				else {
					fieldType.setValue(field.getFieldSubType());
				}
			}
			return columnIntern(field.getName(), field, join);
		}
		finally {
			tlObjectCollector.dispose(remainingJoinNameSB);
			tlObjectCollector.dispose(joinName);
		}
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	@Deprecated
	public IOperand column(String columnName) {
		return column(columnName, null);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	@Deprecated
	public IOperand column(String columnName, ISqlJoin joinClause) {
		return column(columnName, joinClause, true);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperand column(String columnName, ISqlJoin joinClause, boolean checkFieldExistence) {
		ParamChecker.assertParamNotNull(columnName, "columnName");

		ITableMetaData table;
		if (joinClause == null) {
			table = databaseMetaData.getTableByType(entityType);
		}
		else {
			table = databaseMetaData.getTableByName(joinClause.getTableName());
		}
		IFieldMetaData field = table.getFieldByName(columnName);
		if (field == null) {
			if (log.isDebugEnabled()) {
				loggerHistory.debugOnce(log, databaseMetaData,
						"No column '" + columnName + "' found on table '" + table.getName()
								+ "'. This may be a configuration error or usage of deprecated "
								+ IQuery.class.getSimpleName() + " functionality");
			}
		}
		return columnIntern(columnName, field, joinClause);
	}

	protected IOperand columnIntern(String fieldName, IFieldMetaData field, ISqlJoin joinClause) {
		ParamChecker.assertTrue(fieldName != null || field != null,
				"either fieldName or field must be valid");
		try {
			IBeanRuntime<SqlColumnOperand> br = getBeanContext().registerBean(SqlColumnOperand.class)
					.propertyValue("ColumnName", field == null ? fieldName : field.getName())
					.propertyValue("TableAliasHolder", tableAliasHolder);
			if (field != null) {
				br.propertyValue("ColumnType", field.getFieldType());
				br.propertyValue("ColumnSubType", field.getFieldSubType());
				if (field.getMember() != null) {
					br.propertyValue("EntityType", field.getTable().getEntityType());
					br.propertyValue("PropertyName", field.getMember().getName());
				}
			}
			if (joinClause != null) {
				br.propertyValue("JoinClause", joinClause);
			}
			return br.finish();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperator contains(IOperand leftOperand, IOperand rightOperand) {
		return contains(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator contains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlContainsOperator.class, leftOperand, rightOperand,
				caseSensitive);
	}

	@Override
	public IOperator endsWith(IOperand leftOperand, IOperand rightOperand) {
		return endsWith(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator endsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlEndsWithOperator.class, leftOperand, rightOperand,
				caseSensitive);
	}

	@Override
	public IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand) {
		return contains(rightOperand, leftOperand, null);
	}

	@Override
	public IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand,
			Boolean caseSensitive) {
		return contains(rightOperand, leftOperand, caseSensitive);
	}

	@Override
	public IOperator isIn(IOperand leftOperand, IOperand rightOperand) {
		return isIn(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		if (!(rightOperand instanceof IMultiValueOperand)
				&& !(rightOperand instanceof SqlSubselectOperand)) {
			throw new IllegalArgumentException("rightOperand must be an instance of "
					+ IMultiValueOperand.class.getName() + " or a sub-query");
		}
		return createTwoPlaceOperator(SqlIsInOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand) {
		return isEqualTo(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlIsEqualToOperator.class, leftOperand, rightOperand,
				caseSensitive);
	}

	@Override
	public IOperator isGreaterThan(IOperand leftOperand, IOperand rightOperand) {
		return createTwoPlaceOperator(SqlIsGreaterThanOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isGreaterThanOrEqualTo(IOperand leftOperand, IOperand rightOperand) {
		return createTwoPlaceOperator(SqlIsGreaterThanOrEqualToOperator.class, leftOperand,
				rightOperand, null);
	}

	@Override
	public IOperator isLessThan(IOperand leftOperand, IOperand rightOperand) {
		return createTwoPlaceOperator(SqlIsLessThanOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isLessThanOrEqualTo(IOperand leftOperand, IOperand rightOperand) {
		return createTwoPlaceOperator(SqlIsLessThanOrEqualToOperator.class, leftOperand, rightOperand,
				null);
	}

	@Override
	public IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand) {
		return notContains(rightOperand, leftOperand, null);
	}

	@Override
	public IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand,
			Boolean caseSensitive) {
		return notContains(rightOperand, leftOperand, caseSensitive);
	}

	@Override
	public IOperator isNotIn(IOperand leftOperand, IOperand rightOperand) {
		return isNotIn(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isNotIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlIsNotInOperator.class, leftOperand, rightOperand,
				caseSensitive);
	}

	@Override
	public IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand) {
		return isNotEqualTo(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand,
			Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlIsNotEqualToOperator.class, leftOperand, rightOperand,
				caseSensitive);
	}

	@Override
	public IOperator notContains(IOperand leftOperand, IOperand rightOperand) {
		return notContains(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator notContains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlNotContainsOperator.class, leftOperand, rightOperand,
				caseSensitive);
	}

	@Override
	public IOperator isNull(IOperand operand) {
		ParamChecker.assertParamNotNull(operand, "operand");
		return getBeanContext().registerBean(SqlNullCheck.class)//
				.propertyValue("Operand", operand)//
				.propertyValue("IsNull", Boolean.TRUE)//
				.finish();
	}

	@Override
	public IOperator isNotNull(IOperand operand) {
		ParamChecker.assertParamNotNull(operand, "operand");
		return getBeanContext().registerBean(SqlNullCheck.class)//
				.propertyValue("Operand", operand)//
				.propertyValue("IsNull", Boolean.FALSE)//
				.finish();
	}

	@Override
	public IOperator like(IOperand leftOperand, IOperand rightOperand) {
		return like(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator like(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlLikeOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IQueryBuilder<T> limit(IOperand operand) {
		limitOperand = operand;
		return self;
	}

	protected IOperand limitIntern(IOperand operand) {
		ParamChecker.assertParamNotNull(operand, "operand");
		return connectionDialect.getLimitOperand(operand, (IValueOperand) operand);
	}

	@Override
	public IOperator notLike(IOperand leftOperand, IOperand rightOperand) {
		return notLike(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator notLike(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlNotLikeOperator.class, leftOperand, rightOperand,
				caseSensitive);
	}

	@Override
	public IOperand regexpLike(IOperand sourceString, IOperand pattern) {
		return regexpLike(sourceString, pattern, null);
	}

	@Override
	public IOperand regexpLike(IOperand sourceString, IOperand pattern, IOperand matchParameter) {
		ParamChecker.assertParamNotNull(sourceString, "sourceString");
		ParamChecker.assertParamNotNull(pattern, "pattern");
		try {
			return connectionDialect.getRegexpLikeFunction(sourceString, pattern, matchParameter);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperator startsWith(IOperand leftOperand, IOperand rightOperand) {
		return startsWith(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator startsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
		return createTwoPlaceOperator(SqlStartsWithOperator.class, leftOperand, rightOperand,
				caseSensitive);
	}

	@Override
	public IOperand value(Object value) {
		if (value == null) {
			return NullValueOperand.INSTANCE;
		}
		try {
			return getBeanContext().registerBean(DirectValueOperand.class).propertyValue("Value", value)
					.finish();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperand valueName(String paramName) {
		ParamChecker.assertParamNotNull(paramName, "paramName");
		try {
			return getBeanContext().registerBean(SimpleValueOperand.class)
					.propertyValue("ParamName", paramName).finish();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperand all() {
		try {
			return getBeanContext().registerBean(SqlAllOperand.class).finish();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperator fulltext(IOperand queryOperand) {
		return fulltext(this.entityType, queryOperand);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperator fulltext(Class<?> entityType, IOperand queryOperand) {
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(queryOperand, "queryOperand");

		ITableMetaData table = databaseMetaData.getTableByType(entityType);
		List<IFieldMetaData> fulltextFields = table.getFulltextFields();

		IOperator orOperator = null;

		for (int a = fulltextFields.size(); a-- > 0;) {
			IFieldMetaData fulltextField = fulltextFields.get(a);

			Integer label = Integer.valueOf(a + 1);
			IOperand containsFunction = function("CONTAINS",
					columnIntern(fulltextField.getName(), fulltextField, null), queryOperand, value(label));
			IOperator gtOperator = isGreaterThan(containsFunction, value(Integer.valueOf(0)));

			if (orOperator == null) {
				orOperator = gtOperator;
			}
			else {
				orOperator = or(orOperator, gtOperator);
			}
			orderBy(function("SCORE", value(label)), OrderByType.DESC);
		}
		if (fulltextFields.size() == 0) {
			List<IFieldMetaData> primitiveFields = table.getPrimitiveFields();

			IFieldMetaData updatedByField = table.getUpdatedByField();
			IFieldMetaData createdByField = table.getCreatedByField();

			for (int a = primitiveFields.size(); a-- > 0;) {
				IFieldMetaData primitiveField = primitiveFields.get(a);
				if (!String.class.equals(primitiveField.getFieldType())
						|| primitiveField.equals(updatedByField) || primitiveField.equals(createdByField)) {
					continue;
				}
				IOperator containsOperator =
						contains(columnIntern(primitiveField.getName(), primitiveField, null), queryOperand,
								Boolean.FALSE);

				if (orOperator == null) {
					orOperator = containsOperator;
				}
				else {
					orOperator = or(orOperator, containsOperator);
				}
			}
			if (orOperator == null) {
				throw new IllegalStateException(
						"No fulltext column found on table '" + table.getName() + "'");
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug(
							"Building LIKE-based sql as a necessary fallback for missing fulltext-capable columns in table '"
									+ table.getName() + "'");
				}
			}
		}
		if (!this.entityType.equals(entityType)) {
			return join(entityType, orOperator, JoinType.LEFT);
		}
		return orOperator;
	}

	@Override
	public IOperand function(String name, IOperand... operands) {
		ParamChecker.assertParamNotNull(name, "name");
		ParamChecker.assertParamNotNull(operands, "operands");
		try {
			return getBeanContext().registerBean(SqlFunctionOperand.class).propertyValue("Name", name)
					.propertyValue("Operands", operands).finish();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IQueryBuilder<T> orderBy(IOperand operand, OrderByType orderByType) {
		ParamChecker.assertParamNotNull(operand, "operand");
		ParamChecker.assertParamNotNull(orderByType, "orderByType");
		IOperand orderByOperand = getBeanContext().registerBean(SqlOrderByOperator.class)
				.propertyValue("Column", operand).propertyValue("OrderByType", orderByType).finish();

		if (orderByOperands == null) {
			orderByOperands = new ArrayList<>();
		}
		orderByOperands.add(orderByOperand);
		return self;
	}

	@Override
	public IOperand overlaps(IOperand leftOperand, IOperand rightOperand) {
		ParamChecker.assertParamNotNull(leftOperand, "leftOperand");
		ParamChecker.assertParamNotNull(rightOperand, "rightOperand");
		return beanContext.registerBean(OverlapsOperand.class).propertyValue("LeftOperand", leftOperand)
				.propertyValue("RightOperand", rightOperand).finish();
	}

	@Override
	public IQueryBuilder<T> groupBy(IOperand... operand) {
		ParamChecker.assertParamNotNull(operand, "operand");
		if (groupByOperands == null) {
			groupByOperands = new ArrayList<>();
		}
		groupByOperands.addAll(operand);
		return self;
	}

	@Override
	public IOperand interval(IOperand lowerBoundary, IOperand upperBoundary) {
		ParamChecker.assertParamNotNull(lowerBoundary, "lowerBoundary");
		ParamChecker.assertParamNotNull(upperBoundary, "upperBoundary");
		return beanContext.registerBean(IntervalOperand.class)
				.propertyValue("LowerBoundary", lowerBoundary).propertyValue("UpperBoundary", upperBoundary)
				.finish();
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	@Deprecated
	public int selectColumn(String columnName) {
		return selectColumn(columnName, null);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	@Deprecated
	public int selectColumn(String columnName, ISqlJoin join) {
		ParamChecker.assertParamNotNull(columnName, "columnName");
		IOperand columnOperand = column(columnName, join, true);
		return selectColumnIntern(columnOperand);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public int selectProperty(String propertyName) {
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		IOperand propertyOperand = property(propertyName);
		return selectColumnIntern(propertyOperand);
	}

	@Override
	public int select(IOperand operand) {
		return selectColumnIntern(operand);
	}

	protected int selectColumnIntern(IOperand columnOperand) {
		if (selectOperands == null) {
			selectOperands = new ArrayList<>();
		}
		IOperand additionalSelectOperand =
				getBeanContext().registerBean(SqlAdditionalSelectOperand.class)//
						.propertyValue("Column", columnOperand)//
						.finish();
		selectOperands.add(additionalSelectOperand);
		return selectOperands.size() - 1;
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperator clause) {
		return join(entityType, clause, JoinType.LEFT);
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperator clause, JoinType joinType) {
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(clause, "clause");
		ITableMetaData table = databaseMetaData.getTableByType(entityType);
		try {
			return getBeanContext().registerBean(SqlJoinOperator.class)
					.propertyValue("TableName", table.getName())
					.propertyValue("FullqualifiedEscapedTableName", table.getFullqualifiedEscapedName())
					.propertyValue("Clause", clause).propertyValue("JoinType", joinType).finish();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined) {
		return join(entityType, columnBase, columnJoined, JoinType.LEFT);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined,
			JoinType joinType) {
		ParamChecker.assertParamNotNull(entityType, "entityType");
		String tableName = databaseMetaData.getTableByType(entityType).getName();
		return joinIntern(tableName, columnBase, columnJoined, joinType, null);
	}

	@Override
	public ISqlJoin joinIntern(String tableName, IOperand columnBase, IOperand columnJoined,
			JoinType joinType, IBeanContextFactory childContextFactory) {
		ParamChecker.assertNotNull(tableName, "tableName");
		ParamChecker.assertFalse(tableName.isEmpty(), "tableName.isNotEmpty");
		ParamChecker.assertParamNotNull(columnBase, "columnBase");
		ParamChecker.assertTrue(columnBase instanceof SqlColumnOperand, "columnBase type");
		ParamChecker.assertParamNotNull(columnJoined, "columnJoined");
		ParamChecker.assertTrue(columnJoined instanceof SqlColumnOperand, "columnJoined type");

		Class<?> entityType = null;
		ITableMetaData table = databaseMetaData.getTableByName(tableName);
		String fullqualifiedEscapedTableName = tableName;
		if (table != null) {
			entityType = table.getEntityType();
			fullqualifiedEscapedTableName = table.getFullqualifiedEscapedName();
		}
		else {
			fullqualifiedEscapedTableName = sqlBuilder.escapeName(tableName);
		}
		if (entityType != null) {
			relatedEntityTypes.add(entityType);
		}
		try {
			SqlJoinOperator joinClause;
			if (childContextFactory != null) {
				joinClause = (SqlJoinOperator) childContextFactory.registerBean(SqlJoinOperator.class)
						.propertyValue("JoinType", joinType)
						.propertyValue("FullqualifiedEscapedTableName", fullqualifiedEscapedTableName)
						.propertyValue("Clause", isEqualTo(columnBase, columnJoined))
						.propertyValue("JoinedColumn", columnJoined).getInstance();
				joinClause.setTableName(tableName);
			}
			else {
				joinClause = getBeanContext().registerBean(SqlJoinOperator.class)
						.propertyValue("JoinType", joinType).propertyValue("TableName", tableName)
						.propertyValue("FullqualifiedEscapedTableName", fullqualifiedEscapedTableName)
						.propertyValue("Clause", isEqualTo(columnBase, columnJoined))
						.propertyValue("JoinedColumn", columnJoined).finish();
			}
			((SqlColumnOperand) columnJoined).setJoinClause(joinClause);
			return joinClause;
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public <S> IOperand subQuery(ISubQuery<S> subQuery, IOperand... selectedColumns) {
		ParamChecker.assertParamNotNull(subQuery, "subQuery");
		ParamChecker.assertParamOfType(subQuery, "subQuery type", ISubQueryIntern.class);
		ParamChecker.assertParamNotNull(selectedColumns, "selectedColumns");

		((ISubQueryIntern) subQuery).reAlias(tableAliasProvider);

		SqlColumnOperand[] columns = new SqlColumnOperand[selectedColumns.length];
		System.arraycopy(selectedColumns, 0, columns, 0, selectedColumns.length);

		SqlSubselectOperand subQueryOperand = getBeanContext().registerBean(SqlSubselectOperand.class)
				.propertyValue("SelectedColumns", columns).propertyValue("SubQuery", subQuery).finish();

		subQueries.add(subQueryOperand);

		return subQueryOperand;
	}

	@Override
	public IOperand sum(IOperand... summands) {
		return beanContext.registerBean(SumOperand.class).propertyValue("Operands", summands).finish();
	}

	@Override
	public IQuery<T> build() {
		return build(all());
	}

	@Override
	public IPagingQuery<T> buildPaging() {
		return buildPaging(all());
	}

	@Override
	public ISubQuery<T> buildSubQuery() {
		return buildSubQuery(all());
	}

	protected ISqlJoin[] getJoins() {
		if (joinMap.size() == 0) {
			return emptyJoins;
		}
		return joinMap.toArray(ISqlJoin.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQuery<T> build(IOperand whereClause) {
		return (IQuery<T>) build(whereClause, getJoins(), QueryType.DEFAULT);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IPagingQuery<T> buildPaging(IOperand whereClause) {
		return (IPagingQuery<T>) build(whereClause, getJoins(), QueryType.PAGING);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ISubQuery<T> buildSubQuery(IOperand whereClause) {
		return (ISubQuery<T>) build(whereClause, getJoins(), QueryType.SUBQUERY);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQuery<T> build(IOperand whereClause, ISqlJoin... joinClauses) {
		return (IQuery<T>) build(whereClause, joinClauses, QueryType.DEFAULT);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IPagingQuery<T> buildPaging(IOperand whereClause, ISqlJoin... joinClauses) {
		return (IPagingQuery<T>) build(whereClause, joinClauses, QueryType.PAGING);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ISubQuery<T> buildSubQuery(IOperand whereClause, ISqlJoin... joinClauses) {
		return (ISubQuery<T>) build(whereClause, joinClauses, QueryType.SUBQUERY);
	}

	@SuppressWarnings("unchecked")
	protected Object build(IOperand whereClause, final ISqlJoin[] joinClauses,
			final QueryType queryType) {
		if (whereClause instanceof SqlAllOperand) {
			whereClause = null;
		}
		ParamChecker.assertParamNotNull(joinClauses, "joinClauses");

		IServiceContext localContext = null;
		try {
			if (QueryType.PAGING == queryType
					&& (orderByOperands == null || orderByOperands.size() == 0)) {
				// Default order by id ASC if nothing else is explicitly specified
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
				self.orderBy(self.property(metaData.getIdMember().getName()), OrderByType.ASC);
			}
			IValueOperand limitOperandForFramework =
					getBeanContext().registerBean(SimpleValueOperand.class)
							.propertyValue("ParamName", QueryConstants.LIMIT_VALUE)
							.propertyValue("TryOnly", Boolean.TRUE).finish();
			IValueOperand[] limitOperands = {limitOperandForFramework, (IValueOperand) limitOperand};
			final IOperand findFirstValueLimitOperand =
					limitIntern(getBeanContext().registerBean(FindFirstValueOperand.class)
							.propertyValue("operands", limitOperands).finish());
			final IOperand[] groupByOperandArray = groupByOperands != null
					? groupByOperands.toArray(new IOperand[groupByOperands.size()]) : emptyOperands;
			final IOperand[] orderByOperandArray = orderByOperands != null
					? orderByOperands.toArray(new IOperand[orderByOperands.size()]) : emptyOperands;
			final IOperand[] selectArray = selectOperands != null
					? selectOperands.toArray(new IOperand[selectOperands.size()]) : emptyOperands;
			final IList<Class<?>> relatedEntityTypesList = relatedEntityTypes.toList();
			final String queryDelegateName = "queryDelegate", pagingQueryName = "pagingQuery",
					queryName = "query";
			final IOperand fWhereClause = whereClause;

			IServiceContext beanContext = getBeanContext();
			// Build a context from the PARENT of the SqlQueryBuilder-Context. Because from now on the
			// query has a
			// DIFFERENT, own lifecycle
			localContext = (disposeContextOnDispose ? beanContext.getParent() : beanContext)
					.createService("sqlQuery", new IBackgroundWorkerParamDelegate<IBeanContextFactory>() {

						@Override
						public void invoke(IBeanContextFactory childContextFactory) {
							IOperand currWhereClause = fWhereClause;

							IBeanConfiguration whereClauseConf = null;
							IList<ISqlJoin> allJoinClauses = new ArrayList<>(joinClauses);
							for (IQueryBuilderExtension queryBuilderExtension : queryBuilderExtensions) {
								IBeanConfiguration currWhereClauseConf = queryBuilderExtension.applyOnWhereClause(
										childContextFactory, self, currWhereClause, allJoinClauses, queryType);
								if (currWhereClauseConf == null) {
									continue;
								}
								currWhereClause = (IOperand) currWhereClauseConf.getInstance();
								whereClauseConf = currWhereClauseConf;
							}
							if (allJoinClauses.size() == 0) {
								allJoinClauses = EmptyList.getInstance();
							}
							for (int i = 0; i < allJoinClauses.size(); i++) {
								((SqlJoinOperator) allJoinClauses.get(i))
										.setTableAlias(tableAliasProvider.getNextJoinAlias());
							}
							IBeanConfiguration stringQuery = childContextFactory.registerBean(StringQuery.class)//
									.propertyValue("EntityType", SqlQueryBuilder.this.entityType)//
									.propertyValue("JoinClauses", joinClauses)//
									.propertyValue("AllJoinClauses", allJoinClauses.toArray(ISqlJoin.class));

							IBeanConfiguration query = childContextFactory.registerBean(queryName, Query.class)//
									.propertyValue("EntityType", entityType)//
									.propertyRefs(stringQuery)//
									.propertyRef("TransactionalQuery", queryDelegateName)//
									.propertyValue("GroupByOperands", groupByOperandArray)//
									.propertyValue("OrderByOperands", orderByOperandArray)//
									.propertyValue("LimitOperand", findFirstValueLimitOperand)//
									.propertyValue("QueryBuilderExtensions", queryBuilderExtensions)//
									.propertyValue("RelatedEntityTypes", relatedEntityTypesList)//
									.propertyValue("SelectOperands", selectArray)//
									.propertyValue("TableAliasHolder", tableAliasHolder)//
									.propertyValue("ContainsSubQuery", !subQueries.isEmpty());
							if (whereClauseConf != null) {
								stringQuery.propertyRef("RootOperand", whereClauseConf);
								query.propertyRef("RootOperand", whereClauseConf);
							}
							else {
								stringQuery.propertyValue("RootOperand", currWhereClause);
								query.propertyValue("RootOperand", currWhereClause);
							}
							Object queryInstance = query.getInstance();
							childContextFactory.registerBean(queryDelegateName, QueryDelegate.class)//
									.propertyValue("Query", queryInstance)//
									.propertyValue("QueryIntern", queryInstance)//
									.propertyRef("TransactionalQuery", queryName);
							if (QueryType.PAGING == queryType) {
								childContextFactory.registerBean(pagingQueryName, PagingQuery.class)
										.propertyRef("Query", queryDelegateName);
							}
						}
					});
			switch (queryType) {
				case PAGING: {
					IPagingQuery<T> pagingQuery = (IPagingQuery<T>) localContext.getService(pagingQueryName);
					return garbageProxyFactory.createGarbageProxy(pagingQuery, IPagingQuery.class);
				}
				case SUBQUERY: {
					ISubQuery<T> subQuery = localContext.getService("query", ISubQuery.class);
					SubQuery<T> realSubQuery =
							new SubQuery<>(subQuery, joinClauses, subQueries.toArray(SqlSubselectOperand.class));
					return garbageProxyFactory.createGarbageProxy(realSubQuery, ISubQuery.class,
							ISubQueryIntern.class);
				}
				case DEFAULT: {
					IQuery<T> query = (IQuery<T>) localContext.getService(queryDelegateName);
					return garbageProxyFactory.createGarbageProxy(query, IQuery.class);
				}
				default:
					throw RuntimeExceptionUtil.createEnumNotSupportedException(queryType);
			}
		}
		catch (Throwable e) {
			if (localContext != null) {
				localContext.dispose();
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
