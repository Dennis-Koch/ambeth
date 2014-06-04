package de.osthus.ambeth.query.sql;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.criteria.JoinType;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.filter.PagingQuery;
import de.osthus.ambeth.filter.PagingQueryWeakReference;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.proxy.Self;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.PersistenceContext.PersistenceContextType;
import de.osthus.ambeth.query.BasicTwoPlaceOperator;
import de.osthus.ambeth.query.IMultiValueOperand;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.ISqlJoin;
import de.osthus.ambeth.query.ISubQuery;
import de.osthus.ambeth.query.OrderByType;
import de.osthus.ambeth.query.Query;
import de.osthus.ambeth.query.QueryDelegate;
import de.osthus.ambeth.query.QueryWeakReference;
import de.osthus.ambeth.query.StringQuery;
import de.osthus.ambeth.query.SubQueryWeakReference;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamChecker;

@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class SqlQueryBuilder<T> implements IInitializingBean, IQueryBuilder<T>
{
	@LogInstance
	private ILogger log;

	private static enum QueryType
	{
		DEFAULT, PAGING, SUBQUERY;
	}

	protected static final List<ISqlJoin> emptyJoins = Collections.<ISqlJoin> emptyList();

	protected static final Pattern PATTERN_CONTAINS_JOIN = Pattern.compile("\\.");

	protected static final Pattern PATTERN_ALLOWED_SEPARATORS = Pattern.compile("[\\.\\s]+");

	protected static final Pattern PATTERN_ENTITY_NAME_WITH_MARKER = Pattern.compile("([^A-Z]*[A-Z][^\\.]*)#");

	protected IQueryBuilder<T> self;

	protected final LinkedHashMap<String, ISqlJoin> joinMap = new LinkedHashMap<String, ISqlJoin>();

	protected final IList<SqlSubselectOperand> subQueries = new ArrayList<SqlSubselectOperand>();

	protected Class<?> entityType;

	protected IList<IOperand> orderByOperands;

	protected IList<IOperand> selectOperands;

	protected IServiceContext beanContext;

	protected IDatabase database;

	protected boolean disposeContextOnDispose = true;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IThreadLocalObjectCollector objectCollector;

	protected IQueryBuilderFactory qbf;

	protected ITableAliasProvider tableAliasProvider;

	protected ITableAliasHolder tableAliasHolder = new TableAliasHolder();

	protected final LinkedHashSet<Class<?>> relatedEntityTypes = new LinkedHashSet<Class<?>>();

	protected boolean disposed = false;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(entityType, "entityType");
		ParamChecker.assertFalse(disposed, "disposed");

		ParamChecker.assertNotNull(beanContext, "beanContext");
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(qbf, "qbf");
		ParamChecker.assertNotNull(tableAliasProvider, "tableAliasProvider");

		relatedEntityTypes.add(entityType);
		tableAliasHolder.setTableAlias(tableAliasProvider.getNextSubQueryAlias());
	}

	@Override
	public synchronized void dispose()
	{
		if (disposed)
		{
			return;
		}
		disposed = true;
		entityType = null;
		relatedEntityTypes.clear();
		joinMap.clear();
		orderByOperands = null;
		selectOperands = null;
		if (disposeContextOnDispose)
		{
			beanContext.dispose();
		}
		beanContext = null;
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	public void setDisposeContextOnDispose(boolean disposeContextOnDispose)
	{
		this.disposeContextOnDispose = disposeContextOnDispose;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory qbf)
	{
		this.qbf = qbf;
	}

	public void setTableAliasProvider(ITableAliasProvider tableAliasProvider)
	{
		this.tableAliasProvider = tableAliasProvider;
	}

	@Self
	public void setSelf(IQueryBuilder<T> self)
	{
		this.self = self;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	protected IServiceContext getBeanContext()
	{
		if (disposed)
		{
			throw new IllegalStateException("This query builder already is finalized!");
		}
		return beanContext;
	}

	protected BasicTwoPlaceOperator createTwoPlaceOperator(Class<? extends BasicTwoPlaceOperator> operatorType, Object leftOperand, Object rightOperand,
			Boolean caseSensitive)
	{
		ParamChecker.assertParamNotNull(operatorType, "operatorType");
		ParamChecker.assertParamNotNull(leftOperand, "leftOperand");
		ParamChecker.assertParamNotNull(rightOperand, "rightOperand");
		try
		{
			IBeanRuntime<? extends BasicTwoPlaceOperator> operatorBC = beanContext.registerAnonymousBean(operatorType)
					.propertyValue("LeftOperand", leftOperand).propertyValue("RightOperand", rightOperand);
			if (caseSensitive != null)
			{
				operatorBC.propertyValue("CaseSensitive", caseSensitive);
			}
			return operatorBC.finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperator and(IOperand leftOperand, IOperand rightOperand)
	{
		return createTwoPlaceOperator(SqlAndOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator and(IOperand... operands)
	{
		if (operands.length == 0)
		{
			return trueOperator();
		}
		if (operands.length == 1)
		{
			if (operands[0] instanceof IOperator)
			{
				return (IOperator) operands[0];
			}
			else
			{
				throw new IllegalArgumentException("Cannot build 'and' statement with just one operand");
			}
		}

		IOperator currentOperator = and(operands[0], operands[1]);
		for (int i = operands.length; i-- > 2;)
		{
			IOperand operand = operands[i];
			currentOperator = and(currentOperator, operand);
		}

		return currentOperator;
	}

	@Override
	public IOperator or(IOperand leftOperand, IOperand rightOperand)
	{
		return createTwoPlaceOperator(SqlOrOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator or(IOperand... operands)
	{
		if (operands.length == 0)
		{
			return falseOperator();
		}
		if (operands.length == 1)
		{
			if (operands[0] instanceof IOperator)
			{
				return (IOperator) operands[0];
			}
			else
			{
				throw new IllegalArgumentException("Cannot build 'or' statement with just one operand");
			}
		}

		IOperator currentOperator = or(operands[0], operands[1]);
		for (int i = operands.length; i-- > 2;)
		{
			IOperand operand = operands[i];
			currentOperator = or(currentOperator, operand);
		}

		return currentOperator;
	}

	@Override
	public IOperator trueOperator()
	{
		return and(value(1), value(1));
	}

	@Override
	public IOperator falseOperator()
	{
		return and(value(1), value(2));
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperand property(String propertyName)
	{
		return property(propertyName, JoinType.LEFT);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperand property(String propertyName, JoinType joinType)
	{
		return property(propertyName, joinType, null);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperand property(String propertyName, JoinType joinType, IParamHolder<Class<?>> fieldType)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder joinName = tlObjectCollector.create(StringBuilder.class);
		StringBuilder remainingJoinNameSB = tlObjectCollector.create(StringBuilder.class);
		ArrayList<String> propertyByJoinHierarchyList = new ArrayList<String>();
		ArrayList<Class<?>> stepViaEntity = new ArrayList<Class<?>>();
		ArrayList<Boolean> stepReverse = new ArrayList<Boolean>();
		propertyName = PATTERN_ALLOWED_SEPARATORS.matcher(propertyName).replaceAll(".");
		try
		{
			Class<?> entityType = this.entityType;

			Class<?> currentEntityType = entityType;
			remainingJoinNameSB.append(propertyName);
			while (remainingJoinNameSB.length() > 0)
			{
				String remainingJoinName = remainingJoinNameSB.toString();
				remainingJoinNameSB.setLength(0);
				joinName.setLength(0);
				joinName.append(remainingJoinName);
				while (true)
				{
					String currentPropertyName = joinName.toString();
					IEntityMetaData metaData = entityMetaDataProvider.getMetaData(currentEntityType, true);
					if (metaData == null)
					{
						propertyByJoinHierarchyList.add(currentPropertyName);
						remainingJoinNameSB.setLength(0);
						break;
					}
					relatedEntityTypes.add(currentEntityType);
					ITypeInfoItem member = metaData.getMemberByName(currentPropertyName);
					if (member != null)
					{
						stepViaEntity.add(currentEntityType);
						stepReverse.add(Boolean.FALSE);
						currentEntityType = member.getElementType();
						propertyByJoinHierarchyList.add(currentPropertyName);

						if (joinName.length() + 1 <= remainingJoinName.length())
						{
							remainingJoinNameSB.append(remainingJoinName, joinName.length() + 1, remainingJoinName.length());
						}
						else
						{
							IEntityMetaData currentMetaData = entityMetaDataProvider.getMetaData(currentEntityType, true);
							if (currentMetaData != null)
							{
								propertyByJoinHierarchyList.add(currentMetaData.getIdMember().getName());
							}
						}
						break;
					}
					else if (currentPropertyName.startsWith("<"))
					{
						String backwardsPropertyName = currentPropertyName.substring(1);
						String targetEntityName = null;

						if (backwardsPropertyName.contains("#"))
						{
							Matcher matcher = PATTERN_ENTITY_NAME_WITH_MARKER.matcher(backwardsPropertyName);
							if (!matcher.find())
							{
								throw new IllegalArgumentException("Unreadable property join definition: " + propertyName);
							}
							targetEntityName = matcher.group(1);
							backwardsPropertyName = matcher.replaceFirst("");
						}

						Class<?> nextEntityType = null;
						Class<?>[] typesRelatingToThis = metaData.getTypesRelatingToThis();
						for (Class<?> other : typesRelatingToThis)
						{
							if (targetEntityName != null && !targetEntityName.equals(other.getSimpleName()) && !targetEntityName.equals(other.getName()))
							{
								continue;
							}
							IEntityMetaData metaData2 = entityMetaDataProvider.getMetaData(other);
							ITypeInfoItem backwardsMember = metaData2.getMemberByName(backwardsPropertyName);
							if (backwardsMember != null)
							{
								if (nextEntityType != null)
								{
									throw new IllegalStateException("Join via reverse Property not unique: " + propertyName);
								}
								member = backwardsMember;
								nextEntityType = metaData2.getEntityType();
								if (targetEntityName != null)
								{
									break;
								}
							}
						}
						if (member != null)
						{
							currentEntityType = nextEntityType;
							stepViaEntity.add(currentEntityType);
							stepReverse.add(Boolean.TRUE);
							propertyByJoinHierarchyList.add(backwardsPropertyName);

							if (joinName.length() + 1 <= remainingJoinName.length())
							{
								remainingJoinNameSB.append(remainingJoinName, joinName.length() + 1, remainingJoinName.length());
							}
							else
							{
								IEntityMetaData currentMetaData = entityMetaDataProvider.getMetaData(currentEntityType);
								propertyByJoinHierarchyList.add(currentMetaData.getIdMember().getName());
							}
							break;
						}
					}
					int lastIndexOfDot = joinName.lastIndexOf(".");
					if (lastIndexOfDot == -1)
					{
						throw new IllegalArgumentException("No property with name '" + remainingJoinName + "' found on entity '" + currentEntityType.getName()
								+ "'");
					}
					joinName.setLength(lastIndexOfDot);
				}
			}
			joinName.setLength(0);
			ISqlJoin join = null;

			int i = 0;
			for (; i < propertyByJoinHierarchyList.size() - 1; i++)
			{
				String joinPart = propertyByJoinHierarchyList.get(i);
				entityType = stepViaEntity.get(i);
				ITable table = database.getTableByType(entityType);
				if (joinName.length() > 0)
				{
					joinName.append('.');
				}
				joinName.append(joinPart);
				ISqlJoin prevJoin = join;

				IDirectedLink dLink = table.getLinkByMemberName(joinPart);
				if (dLink == null)
				{
					throw new IllegalArgumentException("Property not mapped: " + joinPart);
				}
				Boolean reverse = stepReverse.get(i);
				if (reverse)
				{
					dLink = dLink.getReverse();
				}
				entityType = dLink.getToTable().getEntityType();

				String joinKey = joinName.toString();
				if (reverse)
				{
					joinKey = entityType.getName() + "#" + joinKey;
				}
				join = joinMap.get(joinKey);
				if (join != null)
				{
					continue;
				}
				IField currentFromField = dLink.getFromField();
				IField currentToField = dLink.getToField();

				if (dLink.getLink().hasLinkTable())
				{
					Class<?> fromEntityType = dLink.getFromEntityType();
					byte fromMemberIdIndex = dLink.getFromField().getIdIndex();
					IEntityMetaData fromMetaData = entityMetaDataProvider.getMetaData(fromEntityType);
					String fromMemberName = fromMetaData.getIdMemberByIdIndex(fromMemberIdIndex).getName();
					IField fromField = dLink.getFromTable().getFieldByPropertyName(fromMemberName);
					IOperand columnBase = columnIntern(fromField.getName(), fromField, prevJoin);
					join = join(dLink.getLink().getName(), columnBase, columnIntern(currentFromField.getName(), currentFromField, null), joinType);

					joinMap.put(joinName.toString() + ".link", join);

					prevJoin = join;
					IEntityMetaData toMetaData = entityMetaDataProvider.getMetaData(fromEntityType);
					byte toMemberIdIndex = dLink.getToField().getIdIndex();
					String toMemberName = toMetaData.getIdMemberByIdIndex(toMemberIdIndex).getName();
					IField toField = dLink.getToTable().getFieldByPropertyName(toMemberName);
					currentFromField = currentToField;
					currentToField = toField;
				}

				IOperand columnBase = columnIntern(currentFromField.getName(), currentFromField, prevJoin);
				IOperand columnTarget = columnIntern(currentToField.getName(), currentToField, null);
				join = join(entityType, columnBase, columnTarget, joinType);

				joinMap.put(joinKey, join);
			}

			ITable table = database.getTableByType(entityType);
			IField field = table.getFieldByPropertyName(propertyByJoinHierarchyList.get(i));
			if (field == null)
			{
				throw new IllegalArgumentException("Property not mapped: " + propertyName);
			}
			if (fieldType != null)
			{
				Class<?> currentFieldType = field.getFieldType();
				if (!java.sql.Array.class.isAssignableFrom(currentFieldType))
				{
					fieldType.setValue(currentFieldType);
				}
				else
				{
					fieldType.setValue(field.getFieldSubType());
				}
			}
			return columnIntern(field.getName(), field, join);
		}
		finally
		{
			tlObjectCollector.dispose(remainingJoinNameSB);
			tlObjectCollector.dispose(joinName);
		}
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	@Deprecated
	public IOperand column(String columnName)
	{
		return column(columnName, null);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	@Deprecated
	public IOperand column(String columnName, ISqlJoin joinClause)
	{
		ParamChecker.assertParamNotNull(columnName, "columnName");

		ITable table;
		if (joinClause == null)
		{
			table = database.getTableByType(entityType);
		}
		else
		{
			table = database.getTableByName(joinClause.getTableName());
		}
		IField field = table.getFieldByName(columnName);
		if (field == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("No column '" + columnName + "' found on table '" + table.getName() + "'. This may be a configuration error or usage of deprecated "
						+ IQuery.class.getSimpleName() + " functionality");
			}
		}
		return columnIntern(columnName, field, joinClause);
	}

	protected IOperand columnIntern(String fieldName, IField field, ISqlJoin joinClause)
	{
		ParamChecker.assertTrue(fieldName != null || field != null, "either fieldName or field must be valid");
		try
		{
			IBeanRuntime<SqlColumnOperand> br = getBeanContext().registerAnonymousBean(SqlColumnOperand.class)
					.propertyValue("ColumnName", field == null ? fieldName : field.getName()).propertyValue("TableAliasHolder", tableAliasHolder);
			if (field != null)
			{
				br.propertyValue("ColumnType", field.getFieldType());
				br.propertyValue("ColumnSubType", field.getFieldSubType());
				if (field.getMember() != null)
				{
					br.propertyValue("EntityType", field.getTable().getEntityType());
					br.propertyValue("PropertyName", field.getMember().getName());
				}
			}
			if (joinClause != null)
			{
				br.propertyValue("JoinClause", joinClause);
			}
			return br.finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperator contains(IOperand leftOperand, IOperand rightOperand)
	{
		return contains(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator contains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlContainsOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator endsWith(IOperand leftOperand, IOperand rightOperand)
	{
		return endsWith(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator endsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlEndsWithOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand)
	{
		return contains(rightOperand, leftOperand, null);
	}

	@Override
	public IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return contains(rightOperand, leftOperand, caseSensitive);
	}

	@Override
	public IOperator isIn(IOperand leftOperand, IOperand rightOperand)
	{
		return isIn(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		if (!(rightOperand instanceof IMultiValueOperand) && !(rightOperand instanceof SqlSubselectOperand))
		{
			throw new IllegalArgumentException("rightOperand must be an instance of " + IMultiValueOperand.class.getName() + " or a sub-query");
		}
		return createTwoPlaceOperator(SqlIsInOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.query.sql.IQueryBuilder#isEqualTo(de.osthus.ambeth.query .IOperand, de.osthus.ambeth.query.IOperand)
	 */
	@Override
	public IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return isEqualTo(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlIsEqualToOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isGreaterThan(IOperand leftOperand, IOperand rightOperand)
	{
		return createTwoPlaceOperator(SqlIsGreaterThanOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isGreaterThanOrEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return createTwoPlaceOperator(SqlIsGreaterThanOrEqualToOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isLessThan(IOperand leftOperand, IOperand rightOperand)
	{
		return createTwoPlaceOperator(SqlIsLessThanOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isLessThanOrEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return createTwoPlaceOperator(SqlIsLessThanOrEqualToOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand)
	{
		return notContains(rightOperand, leftOperand, null);
	}

	@Override
	public IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return notContains(rightOperand, leftOperand, caseSensitive);
	}

	@Override
	public IOperator isNotIn(IOperand leftOperand, IOperand rightOperand)
	{
		return isNotIn(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isNotIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlIsNotInOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return isNotEqualTo(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlIsNotEqualToOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator notContains(IOperand leftOperand, IOperand rightOperand)
	{
		return notContains(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator notContains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlNotContainsOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isNull(IOperand operand)
	{
		ParamChecker.assertParamNotNull(operand, "operand");
		try
		{
			return getBeanContext().registerAnonymousBean(SqlNullCheck.class).propertyValue("Operand", operand).propertyValue("IsNull", true).finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperator isNotNull(IOperand operand)
	{
		ParamChecker.assertParamNotNull(operand, "operand");
		try
		{
			return getBeanContext().registerAnonymousBean(SqlNullCheck.class).propertyValue("Operand", operand).propertyValue("IsNull", false).finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperator like(IOperand leftOperand, IOperand rightOperand)
	{
		return like(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator like(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlLikeOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator notLike(IOperand leftOperand, IOperand rightOperand)
	{
		return notLike(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator notLike(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlNotLikeOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator startsWith(IOperand leftOperand, IOperand rightOperand)
	{
		return startsWith(leftOperand, rightOperand, null);
	}

	@Override
	public IOperator startsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createTwoPlaceOperator(SqlStartsWithOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperand value(Object value)
	{
		if (value == null)
		{
			return NullValueOperand.INSTANCE;
		}
		try
		{
			return getBeanContext().registerAnonymousBean(DirectValueOperand.class).propertyValue("Value", value).finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperand valueName(String paramName)
	{
		ParamChecker.assertParamNotNull(paramName, "paramName");
		try
		{
			return getBeanContext().registerAnonymousBean(SimpleValueOperand.class).propertyValue("ParamName", paramName).finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperand all()
	{
		try
		{
			return getBeanContext().registerAnonymousBean(SqlAllOperand.class).finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperator fulltext(IOperand queryOperand)
	{
		return fulltext(this.entityType, queryOperand);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOperator fulltext(Class<?> entityType, IOperand queryOperand)
	{
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(queryOperand, "queryOperand");

		ITable table = this.database.getTableByType(entityType);
		List<IField> fulltextFields = table.getFulltextFields();

		IOperator orOperator = null;

		for (int a = fulltextFields.size(); a-- > 0;)
		{
			IField fulltextField = fulltextFields.get(a);

			Integer label = Integer.valueOf(a + 1);
			IOperand containsFunction = function("CONTAINS", columnIntern(fulltextField.getName(), fulltextField, null), queryOperand, value(label));
			IOperator gtOperator = isGreaterThan(containsFunction, value(Integer.valueOf(0)));

			if (orOperator == null)
			{
				orOperator = gtOperator;
			}
			else
			{
				orOperator = or(orOperator, gtOperator);
			}
			orderBy(function("SCORE", value(label)), OrderByType.DESC);
		}
		if (fulltextFields.size() == 0)
		{
			List<IField> primitiveFields = table.getPrimitiveFields();

			IField updatedByField = table.getUpdatedByField();
			IField createdByField = table.getCreatedByField();

			for (int a = primitiveFields.size(); a-- > 0;)
			{
				IField primitiveField = primitiveFields.get(a);
				if (!String.class.equals(primitiveField.getFieldType()) || primitiveField.equals(updatedByField) || primitiveField.equals(createdByField))
				{
					continue;
				}
				IOperator containsOperator = contains(columnIntern(primitiveField.getName(), primitiveField, null), queryOperand, Boolean.FALSE);

				if (orOperator == null)
				{
					orOperator = containsOperator;
				}
				else
				{
					orOperator = or(orOperator, containsOperator);
				}
			}
			if (orOperator == null)
			{
				throw new IllegalStateException("No fulltext column found on table '" + table.getName() + "'");
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("Building LIKE-based sql as a necessary fallback for missing fulltext-capable columns in table '" + table.getName() + "'");
				}
			}
		}
		if (!this.entityType.equals(entityType))
		{
			return join(entityType, orOperator, JoinType.LEFT);
		}
		return orOperator;
	}

	@Override
	public IOperand function(String name, IOperand... parameters)
	{
		ParamChecker.assertParamNotNull(name, "name");
		ParamChecker.assertParamNotNull(parameters, "parameters");
		try
		{
			return getBeanContext().registerAnonymousBean(SqlFunctionOperand.class).propertyValue("Name", name).propertyValue("Parameters", parameters)
					.finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IQueryBuilder<T> orderBy(IOperand operand, OrderByType orderByType)
	{
		ParamChecker.assertParamNotNull(operand, "operand");
		ParamChecker.assertParamNotNull(orderByType, "orderByType");
		IOperand orderByOperand = getBeanContext().registerAnonymousBean(SqlOrderByOperator.class).propertyValue("Column", operand)
				.propertyValue("OrderByType", orderByType).finish();

		if (orderByOperands == null)
		{
			orderByOperands = new ArrayList<IOperand>();
		}
		orderByOperands.add(orderByOperand);
		return this;
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	@Deprecated
	public int selectColumn(String columnName)
	{
		return selectColumn(columnName, null);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	@Deprecated
	public int selectColumn(String columnName, ISqlJoin join)
	{
		ParamChecker.assertParamNotNull(columnName, "columnName");
		IOperand columnOperand = column(columnName, join);
		return selectColumnIntern(columnOperand);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public int selectProperty(String propertyName)
	{
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		IOperand propertyOperand = property(propertyName);
		return selectColumnIntern(propertyOperand);
	}

	protected int selectColumnIntern(IOperand columnOperand)
	{
		if (selectOperands == null)
		{
			selectOperands = new ArrayList<IOperand>();
		}
		IOperand additionalSelectOperand = getBeanContext().registerAnonymousBean(SqlAdditionalSelectOperand.class).propertyValue("Column", columnOperand)
				.finish();
		selectOperands.add(additionalSelectOperand);
		return selectOperands.size() - 1;
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public ISqlJoin join(Class<?> entityType, IOperator clause)
	{
		return join(entityType, clause, JoinType.LEFT);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public ISqlJoin join(Class<?> entityType, IOperator clause, JoinType joinType)
	{
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(clause, "clause");
		String tableName = database.getTableByType(entityType).getName();
		try
		{
			return getBeanContext().registerAnonymousBean(SqlJoinOperator.class).propertyValue("TableName", tableName).propertyValue("Clause", clause)
					.propertyValue("JoinType", joinType).finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined)
	{
		return join(entityType, columnBase, columnJoined, JoinType.LEFT);
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined, JoinType joinType)
	{
		ParamChecker.assertParamNotNull(entityType, "entityType");
		String tableName = database.getTableByType(entityType).getName();
		return join(tableName, columnBase, columnJoined, joinType);
	}

	protected ISqlJoin join(String tableName, IOperand columnBase, IOperand columnJoined, JoinType joinType)
	{
		ParamChecker.assertNotNull(tableName, "tableName");
		ParamChecker.assertFalse(tableName.isEmpty(), "tableName.isNotEmpty");
		ParamChecker.assertParamNotNull(columnBase, "columnBase");
		ParamChecker.assertTrue(columnBase instanceof SqlColumnOperand, "columnBase type");
		ParamChecker.assertParamNotNull(columnJoined, "columnJoined");
		ParamChecker.assertTrue(columnJoined instanceof SqlColumnOperand, "columnJoined type");

		Class<?> entityType = null;
		ITable table = database.getTableByName(tableName);
		if (table != null)
		{
			entityType = table.getEntityType();
		}
		if (entityType != null)
		{
			relatedEntityTypes.add(entityType);
		}
		try
		{
			SqlJoinOperator joinClause = getBeanContext().registerAnonymousBean(SqlJoinOperator.class).propertyValue("JoinType", joinType)
					.propertyValue("TableName", tableName).propertyValue("Clause", isEqualTo(columnBase, columnJoined))
					.propertyValue("JoinedColumn", columnJoined).finish();
			((SqlColumnOperand) columnJoined).setJoinClause(joinClause);
			return joinClause;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public <S> IOperand subQuery(ISubQuery<S> subQuery, IOperand... selectedColumns)
	{
		ParamChecker.assertParamNotNull(subQuery, "subQuery");
		ParamChecker.assertParamOfType(subQuery, "subQuery type", SubQueryWeakReference.class);
		ParamChecker.assertParamNotNull(selectedColumns, "selectedColumns");

		((SubQueryWeakReference<S>) subQuery).reAlias(tableAliasProvider);

		SqlColumnOperand[] columns = new SqlColumnOperand[selectedColumns.length];
		System.arraycopy(selectedColumns, 0, columns, 0, selectedColumns.length);

		SqlSubselectOperand subQueryOperand = getBeanContext().registerAnonymousBean(SqlSubselectOperand.class).propertyValue("SelectedColumns", columns)
				.propertyValue("SubQuery", subQuery).finish();

		subQueries.add(subQueryOperand);

		return subQueryOperand;
	}

	@Override
	public IQuery<T> build()
	{
		return build(all());
	}

	@Override
	public IPagingQuery<T> buildPaging()
	{
		return buildPaging(all());
	}

	@Override
	public ISubQuery<T> buildSubQuery()
	{
		return buildSubQuery(all());
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQuery<T> build(IOperand whereClause)
	{
		return (IQuery<T>) build(whereClause, this.joinMap.values(), QueryType.DEFAULT);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IPagingQuery<T> buildPaging(IOperand whereClause)
	{
		return (IPagingQuery<T>) build(whereClause, this.joinMap.values(), QueryType.PAGING);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ISubQuery<T> buildSubQuery(IOperand whereClause)
	{
		return (ISubQuery<T>) build(whereClause, this.joinMap.values(), QueryType.SUBQUERY);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQuery<T> build(IOperand whereClause, ISqlJoin... joinClauses)
	{
		return (IQuery<T>) buildIntern(whereClause, joinClauses, QueryType.DEFAULT);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IPagingQuery<T> buildPaging(IOperand whereClause, ISqlJoin... joinClauses)
	{
		return (IPagingQuery<T>) buildIntern(whereClause, joinClauses, QueryType.PAGING);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ISubQuery<T> buildSubQuery(IOperand whereClause, ISqlJoin... joinClauses)
	{
		return (ISubQuery<T>) buildIntern(whereClause, joinClauses, QueryType.SUBQUERY);
	}

	protected Object buildIntern(IOperand whereClause, ISqlJoin[] joinClauses, QueryType queryType)
	{
		ParamChecker.assertParamNotNull(whereClause, "operand");
		ParamChecker.assertParamNotNull(joinClauses, "joinClauses");

		List<ISqlJoin> joinList;
		if (joinClauses.length == 0)
		{
			joinList = emptyJoins;
		}
		else
		{
			joinList = Arrays.asList(joinClauses);
		}
		return build(whereClause, joinList, queryType);
	}

	@SuppressWarnings("unchecked")
	protected Object build(final IOperand whereClause, final List<ISqlJoin> joinClauses, final QueryType queryType)
	{
		ParamChecker.assertParamNotNull(whereClause, "whereClause");
		ParamChecker.assertParamNotNull(joinClauses, "joinClauses");

		for (int i = 0; i < joinClauses.size(); i++)
		{
			((SqlJoinOperator) joinClauses.get(i)).setTableAlias(tableAliasProvider.getNextJoinAlias());
		}
		try
		{
			if (QueryType.PAGING == queryType && (orderByOperands == null || orderByOperands.size() == 0))
			{
				// Default order by id ASC if nothing else is explicitly specified
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
				self.orderBy(self.property(metaData.getIdMember().getName()), OrderByType.ASC);
			}
			final IOperand[] orderByOperandArray = orderByOperands != null ? orderByOperands.toArray(new IOperand[orderByOperands.size()]) : null;

			final IOperand[] selectArray = selectOperands != null ? selectOperands.toArray(new IOperand[selectOperands.size()]) : null;

			final IList<Class<?>> relatedEntityTypesList = relatedEntityTypes.toList();

			final String queryDelegateName = "queryDelegate", pagingQueryName = "pagingQuery";

			IServiceContext beanContext = getBeanContext();
			// Build a context from the PARENT of the SqlQueryBuilder-Context. Because from now on the query has a
			// DIFFERENT, own lifecycle
			IServiceContext localContext = (disposeContextOnDispose ? beanContext.getParent() : beanContext).createService("sqlQuery",
					new RegisterPhaseDelegate()
					{

						@Override
						public void invoke(IBeanContextFactory childContextFactory)
						{
							childContextFactory.registerBean("stringQuery", StringQuery.class).propertyValue("EntityType", SqlQueryBuilder.this.entityType)
									.propertyValue("RootOperand", whereClause).propertyValue("JoinClauses", joinClauses);
							Object query = childContextFactory.registerBean("query", Query.class).propertyValue("EntityType", entityType)
									.propertyRefs("stringQuery").propertyRef("TransactionalQuery", queryDelegateName).propertyValue("RootOperand", whereClause)
									.propertyValue("OrderByOperands", orderByOperandArray).propertyValue("SelectOperands", selectArray)
									.propertyValue("RelatedEntityTypes", relatedEntityTypesList).propertyValue("TableAliasHolder", tableAliasHolder)
									.propertyValue("ContainsSubQuery", !subQueries.isEmpty()).getInstance();
							childContextFactory.registerBean(queryDelegateName, QueryDelegate.class).propertyValue("Query", query)
									.propertyRef("TransactionalQuery", "query");
							if (QueryType.PAGING == queryType)
							{
								childContextFactory.registerBean(pagingQueryName, PagingQuery.class).propertyRef("Query", queryDelegateName);
							}
						}
					});
			try
			{
				switch (queryType)
				{
					case PAGING:
						IPagingQuery<T> pagingQuery = (IPagingQuery<T>) localContext.getService(pagingQueryName);
						return new PagingQueryWeakReference<T>(pagingQuery);
					case SUBQUERY:
						ISubQuery<T> subQuery = localContext.getService("query", ISubQuery.class);
						SubQueryWeakReference<T> weakReference = new SubQueryWeakReference<T>(subQuery);
						weakReference.setJoinOperands(joinMap.values());
						weakReference.setSubQueries(subQueries);
						return weakReference;
					default:
					case DEFAULT:
						IQuery<T> query = (IQuery<T>) localContext.getService(queryDelegateName);
						return new QueryWeakReference<T>(query);
				}
			}
			finally
			{
				dispose();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}